// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista

window.MedmijDicomViewer = (function() {
	// TODO: separate stuff (gallery, downloader, cache)
	// TODO: maybe read the kos in javascript?

	// Cache n images before the current index and n images after the current index
	const CACHE_RANGE = 30; // TODO: Also employ RAM limit?
	const MAX_CONCURRENT = 5;
	const MAX_SCALE = 6;
	const MOVE_AMOUNT = 20;
	const ZOOM_BUTTON_STEP = 10;
	
	const MULTIFRAME_MAX_CONCURRENT = 10;
	//const MULTI_FRAME_CACHE = 50; //TODO: Limit how many multiframe images we keep in memory
	const MULTI_FRAME_FRAME_RATE = 12;

	const MIME_TO_EXTENSION = {
	  "image/jpeg": "jpg",
	  "image/png": "png",
	  "image/gif": "gif",
	  "application/pdf": "pdf",
	  "text/plain": "txt",
	  "application/json": "json",
	  "image/webp": "webp",
	};

	// TODO: considering memory consumption it probably doesn't make sense to have more than
	//       1 viewer on a page, so maybe just reduce this to a single instance?
	const viewers = new Map();

	window.addEventListener('unload', function () {
		// Perform cleanup tasks here
		console.debug('Page is unloading, cleaning up viewers');
		clearViewers();
	});

	function initViewer(viewerEl, viewerId) {
		console.debug('Initialising viewer with id=[', viewerId, '] for ', viewerEl);
		const kosId = viewerEl.getAttribute('data-medmij-kos-id');
		const imgEndpoint = viewerEl.getAttribute('data-medmij-img-endpoint');
		const cache = new Map(); // index, { status, url, blob}
		const multiframeCache = new Map(); // frame number, { status, url, blob}

		const prevImage = viewerEl.querySelector('.nav .prev');
		const dispIdx = viewerEl.querySelector('.nav .index');
		const nextImage = viewerEl.querySelector('.nav .next');
		
		const studyFrame = viewerEl.querySelector('.study');
		const seriesFrame = viewerEl.querySelector('div.series');

		const container = seriesFrame.querySelector('.contents');
		const imgElement = container.querySelector('img');
		const containerRect = container.getBoundingClientRect();
		const otherElement = container.querySelector('iframe');
		const multiframeLoader = container.querySelector('.multiframe-loader');
		const multiframeProgress = multiframeLoader.querySelector('.progress');

		const zoomSlider = viewerEl.querySelector('.image-controls .zoom .slider');
		const zoomStep = parseInt(zoomSlider.step);
		const zoomMin = parseInt(zoomSlider.min);
		const zoomMax = parseInt(zoomSlider.max);
		
		let estRamFootprint = 0;

		let seriesId;
		let currSeries;
		let currIdx, maxIdx;
		let preloadQueue = [];
		let activeQueue = [];
		let multiframePreloadQueue = [];
		let multiframeActiveQueue = [];

		let scale;
		let initialDistance = null;
		let isDragging = false;
		let startX, startY;
		let translateX, translateY;
		let scrollLeft, scrollTop;
		
		let preloadedFrames = [];
		let preloadId = 0;
		let timer;

		function initSeries(seriesEl) {
			seriesId = seriesEl.getAttribute('data-medmij-series-id');
			maxIdx = seriesEl.getAttribute('data-medmij-series-len');
			currSeries = [];
			seriesEl.querySelectorAll("template.instance-info").forEach(instance => {
				let instanceId = instance.getAttribute('data-medmij-instance-id');
				let url = `${imgEndpoint}&instanceId=${instanceId}`
				currSeries.push(url);
			});
			currIdx = 0;
			cleanup();
			updateCache(currIdx);
		}

		async function fetchImageBlob(index, dicom) {
			let url = currSeries[index];
			if (dicom == true) {
				// TODO: should probably get the real url (or maybe entire KOS?) from the backend and send that as
				//       parameter to a proxy endpoint. This will allow us to do filtering on SOP class (either in the
				//       backend or in javascript) and removes the need to parse the kos in the backend for every request.
				url = url + '&dicom=true';
			}
			console.debug('Fetch url', url);
			try {
				const response = await fetch(url);
				if (response.status == 401) {
					seriesFrame.classList.toggle('hidden', true); //Makes download queue stop
					document.getElementById('token-error').classList.toggle('hidden', false);
					document.getElementById('bbs-viewer').style.display = 'none';
				}
				return response;
			} catch (error) {
				console.error(`Failed to fetch image at index ${index}:`, error);
				return null;
			}
		}

		async function fetchMultiframeImageBlob(index, frameNumber) {
			let url = currSeries[index] + '&frameNumber=' + frameNumber;
			console.debug('Fetch url', url);
			try {
				const response = await fetch(url);
				if (response.status == 401) {
					seriesFrame.classList.toggle('hidden', true); //Makes download queue stop
					document.getElementById('token-error').classList.toggle('hidden', false);
					document.getElementById('bbs-viewer').style.display = 'none';
				}
				return response;
			} catch (error) {
				console.error(`Failed to fetch image at index ${index} and frame ${frameNumber}:`, error);
				return null;
			}
		}

		async function waitForImage(cacheIdx) {
			return new Promise(resolve => {
				const check = () => {
					const cached = cache.get(cacheIdx);
					if (cached) {
						if (cached.status == 429) {
							enqueuePreload(cacheIdx);
							cache.delete(cacheIdx);
							setTimeout(check, 50);
						} else {
							resolve(cached);
						}
					} else {
						setTimeout(check, 50);
					}
				};
				check();
			});
		}

		async function download(dlIdx, dicom) {
			// TODO: should probably get the real url (or maybe entire KOS?) from the backend and send that as
			//       parameter to a proxy endpoint. This will allow us to do filtering on SOP class (either in the
			//       backend or in javascript) and removes the need to parse the kos in the backend for every request.
			let url = currSeries[dlIdx];
			let filename = `${seriesId}-${dlIdx}`;
			if (dicom == true) {
				url = url + '&dicom=true&_download=true&_filename=' + filename + '.dcm';
			} else {
				url = url + '&_download=true&_filenameWithoutExt=' + filename + '&_overrideContentType=application/octet-stream';
			}
			window.location.href = url;
		}

		function enqueuePreload(index) {
			if (index >= 0 && index < currSeries.length && !cache.has(index) && !preloadQueue.includes(index) && !activeQueue.includes(index)) {
				preloadQueue.push(index);
				processQueue();
			}
		}

		async function processQueue() {
			while (activeQueue.length < MAX_CONCURRENT && preloadQueue.length > 0 && !seriesFrame.classList.contains('hidden')) {
				const index = preloadQueue.shift();
				activeQueue.push(index);

				const response = await fetchImageBlob(index, false);
				if (response) {
					const blob = await response.blob();
					const status = response.status;
					if (response.ok) {
						estRamFootprint = estRamFootprint + blob.size;
						console.debug('Estimated memory footprint:', estRamFootprint, 'b');
						console.debug('Estimated memory footprint:', estRamFootprint/1024, 'kb');
						console.debug('Estimated memory footprint:', estRamFootprint/1024/1024, 'mb');
						const url = URL.createObjectURL(blob);
						cache.set(index, { status, url, blob });
					} else {
						const url = null;
						cache.set(index, { status, url, blob });
					}
				}

				const foundIdx = activeQueue.indexOf(index);
				if (foundIdx != -1) {
					activeQueue.splice(foundIdx, 1);
				}
				processQueue(); // Continue processing
			}
		}

		async function processMultiframeQueue(index, multiframeDownloadId) {
			while (multiframeActiveQueue.length < MULTIFRAME_MAX_CONCURRENT && multiframePreloadQueue.length > 0 && !seriesFrame.classList.contains('hidden') && multiframeDownloadId == preloadId) {
				const frameNumber = multiframePreloadQueue.shift();
				multiframeActiveQueue.push(frameNumber);

				const response = await fetchMultiframeImageBlob(index, frameNumber);
				if (response) {
					const blob = await response.blob();
					const status = response.status;
					if (response.ok) {
						estRamFootprint = estRamFootprint + blob.size;
						console.debug('Estimated memory footprint:', estRamFootprint, 'b');
						console.debug('Estimated memory footprint:', estRamFootprint/1024, 'kb');
						console.debug('Estimated memory footprint:', estRamFootprint/1024/1024, 'mb');
						if (multiframeDownloadId == preloadId) {
							const url = URL.createObjectURL(blob);
							multiframeCache.set(frameNumber, { status, url, blob });
						}
					} else {
						const url = null;
						if(multiframeDownloadId == preloadId) {
							multiframeCache.set(frameNumber, { status, url, blob });
						}
					}
				}

				const foundIdx = multiframeActiveQueue.indexOf(frameNumber);
				if (foundIdx != -1) {
					multiframeActiveQueue.splice(foundIdx, 1);
				}
				processMultiframeQueue(index, multiframeDownloadId); // Continue processing
			}
		}

		async function preloadFrames(index, frameCount, multiframeDownloadId) {
			for (let i = 0; i < frameCount; i++) {
				multiframePreloadQueue.push(i);
			}
			processMultiframeQueue(index, multiframeDownloadId);
			return new Promise(resolve => {
				const check = () => {
					const cached = multiframeCache.size;
					multiframeProgress.innerHTML = '' + cached + ' / ' + frameCount;
					if (cached == frameCount) {
						resolve(cached);
					} else if (multiframeDownloadId != preloadId) {
						resolve(cached);
					} else {
						setTimeout(check, 50);
					}
				};
				check();
			});
		}

		function revokeImage(index) {
			const cached = cache.get(index);
			if (cached) {
				if (cached.blob) {
					estRamFootprint = estRamFootprint - cached.blob.size;
					URL.revokeObjectURL(cached.url);
				}
				cache.delete(index);
			}
		}

		function updateCache(newIndex) {
			currentIndex = newIndex;

			// Revoke images outside the cache range
			for (let key of cache.keys()) {
				if (key < currentIndex - CACHE_RANGE || key > currentIndex + CACHE_RANGE) {
					revokeImage(key);
				}
			}

			// Enqueue images for preloading
			for (let i = currentIndex - CACHE_RANGE; i <= currentIndex + CACHE_RANGE; i++) {
				enqueuePreload(i);
			}
		}

		function updNav() {
			if (currIdx < 2) {
				prevImage.classList.add('disabled');
			}
			if (maxIdx - currIdx < 0) {
				nextImage.classList.add('disabled');
			}
			dispIdx.innerHTML = "" + (currIdx + 1) + " / " + (maxIdx);
		}

		async function showImg(newIdx) {
			if (newIdx >= 0 && newIdx < currSeries.length) {
				clearCache(multiframeCache);
				clearInterval(timer);
				multiframeLoader.classList.toggle('hidden', true);
				multiframeProgress.innerHTML = '';
				currIdx = newIdx;
				updNav();

				zoomSlider.value = 0;
				scale = 1;

				startX = 0;
				startY = 0;
				translateX = 0;
				translateY = 0;
				updateTransform();
				
				updateCache(newIdx);
				imgElement.classList.toggle('hidden', true);
				otherElement.classList.toggle('hidden', true);

				const cached = await waitForImage(newIdx);

				if (currIdx == newIdx) {
					if (cached.blob) {
						if (cached.blob.type == 'application/pdf') {
							console.debug('Displaying pdf:', cached.url);
							otherElement.src = cached.url;
							otherElement.classList.toggle('hidden', false);
							imgElement.classList.toggle('hidden', true);
						} else if (cached.blob.type == 'application/json') {
							console.debug('Displaying multiframe image:', cached.url);
							multiframeLoader.classList.toggle('hidden', false);
							cached.blob.text().then(text => {
								const jsonData = JSON.parse(text);
								console.debug(jsonData);
								let frameCount = jsonData.numberOfFrames;
								let currentFrame = 0;
								// Prevent going back and forth between images from polluting the current preload
								let myPreloadId = ++preloadId;

								function playFrames() {
									if (currIdx == newIdx && myPreloadId == preloadId) {
										multiframeLoader.classList.toggle('hidden', true);
										imgElement.classList.toggle('hidden', false);
										otherElement.classList.toggle('hidden', true);
										timer = setInterval(() => {
											imgElement.src = multiframeCache.get(currentFrame).url;
											currentFrame = (currentFrame + 1) % frameCount;
										}, 1000/MULTI_FRAME_FRAME_RATE);
									}
								}

								preloadFrames(newIdx, frameCount, myPreloadId).then(playFrames);
							});

						} else {
							console.debug('Displaying image:', cached.url);
							imgElement.src = cached.url;
							imgElement.classList.toggle('hidden', false);
							otherElement.classList.toggle('hidden', true);
						}
					} else if (cached.status == 404) {
						// TODO: show 404 image?
						document.getElementById('not-found-error').classList.toggle('hidden');
					} else {
						// TODO: show error image?
						document.getElementById('generic-error').classList.toggle('hidden');
					}
				}
			}
		}

		function zoom(x) {
			scale = 1 + ((x - zoomMin) * (MAX_SCALE - 1)) / (zoomMax - zoomMin);
			updateTransform();
		}

		function getDistance(touches) {
			const [touch1, touch2] = touches;
			const dx = touch2.clientX - touch1.clientX;
			const dy = touch2.clientY - touch1.clientY;
			return Math.sqrt(dx * dx + dy * dy);
		}

		function updateTransform() {
			if(scale == 1) {
				translateX = 0;
				translateY = 0;
			} else {
				const imgWidth = imgElement.naturalWidth;
				const imgHeight = imgElement.naturalHeight;
				const scaledWidth = imgWidth * scale;
				const scaledHeight = imgHeight * scale;
				const maxX = Math.max(0, (scaledWidth - containerRect.width) / 2);
				const maxY = Math.max(0, (scaledHeight - containerRect.height) / 2);
				translateX = Math.min(Math.max(translateX, -maxX), maxX);
				translateY = Math.min(Math.max(translateY, -maxY), maxY);
			}
			imgElement.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scale})`;
		}

		function clearCache(cache) {
			cache.forEach(function (v, k) {
				if (v.blob) {
					URL.revokeObjectURL(v.url);
					estRamFootprint = estRamFootprint - v.blob.size;
					console.debug('Estimated memory footprint:', estRamFootprint, 'b');
					console.debug('Estimated memory footprint:', estRamFootprint/1024, 'kb');
					console.debug('Estimated memory footprint:', estRamFootprint/1024/1024, 'mb');
				}
			});
			cache.clear();
		}

		function cleanup() {
			let preloadQueue = [];
			let activeQueue = [];
			clearCache(cache);
			clearCache(multiframeCache);
		}
		
		viewerEl.querySelectorAll('.nav .close').forEach(closeBtn => {
			closeBtn.addEventListener('click', e => {
				cleanup();
				viewerEl.classList.toggle('hidden', true);
				seriesFrame.classList.toggle('hidden', true); //Makes download queue stop
				currIdx = -1; // Stop the multiframe timer
			});
		});

		studyFrame.querySelectorAll('.series-list .series').forEach(openBtn => {
			openBtn.addEventListener('click', e => {
				studyFrame.classList.toggle('hidden', true);
				seriesFrame.classList.toggle('hidden', false);
				initSeries(openBtn);
				showImg(currIdx);
			});
		});

		seriesFrame.querySelector('.nav .back')
			.addEventListener('click', e => {
				cleanup();
				seriesFrame.classList.toggle('hidden', true); //Makes download queue stop
				studyFrame.classList.toggle('hidden', false);
				currIdx = -1; // Stop the multiframe timer
			});

		seriesFrame.querySelector('.nav .prev')
			.addEventListener('click', e => {
				showImg(currIdx - 1);
			});

		seriesFrame.querySelector('.nav .next')
			.addEventListener('click', e => {
				showImg(currIdx + 1);
			});

		seriesFrame.querySelector('.image-controls .zoom .minus')
			.addEventListener('click', e => {
				let newValue = parseInt(zoomSlider.value) - ZOOM_BUTTON_STEP;
				// Ensure the value doesn't exceed the min
				newValue = Math.max(newValue, zoomMin);

				zoomSlider.value = newValue;
				zoom(newValue);
			});

		seriesFrame.querySelector('.image-controls .zoom .plus')
			.addEventListener('click', e => {
				let newValue = parseInt(zoomSlider.value) + ZOOM_BUTTON_STEP;
				// Ensure the value doesn't exceed the max
				newValue = Math.min(newValue, zoomMax);

				zoomSlider.value = newValue;
				zoom(newValue);
			});

		seriesFrame.querySelector('.download .image')
			.addEventListener('click', e => {
				download(currIdx, false);
			});

		seriesFrame.querySelector('.download .dicom')
			.addEventListener('click', e => {
				seriesFrame.querySelector('.overlay.beeld-confirm').classList.toggle('hidden', false);
			});

		seriesFrame.querySelector('.overlay.beeld-confirm .primary')
			.addEventListener('click', e => {
				seriesFrame.querySelector('.overlay.beeld-confirm').classList.toggle('hidden', true);
				download(currIdx, true);
			});

		seriesFrame.querySelector('.overlay.beeld-confirm .secondary')
			.addEventListener('click', e => {
				seriesFrame.querySelector('.overlay.beeld-confirm').classList.toggle('hidden', true);
			});

		zoomSlider.addEventListener('input', (e) => {
			zoom(e.target.value);
		});

		container.addEventListener('wheel', function (e) {
			console.debug("zoom with mousewheel");
			e.preventDefault(); // Prevent page scroll
			var newValue = parseInt(zoomSlider.value);
			newValue = e.deltaY < 0
				? Math.min(newValue + ZOOM_BUTTON_STEP, zoomMax)
				: Math.max(newValue - ZOOM_BUTTON_STEP, zoomMin);
			zoomSlider.value = newValue;
			zoom(newValue);
		});

		container.addEventListener('touchstart', (e) => {
			if (e.touches.length === 2) {
				initialDistance = getDistance(e.touches);
			} else if (e.touches.length === 1) {
				isDragging = true;
				startX = e.touches[0].clientX - translateX;
				startY = e.touches[0].clientY - translateY;
			}
		});

		container.addEventListener('touchmove', (e) => {
			if (e.touches.length === 2 && initialDistance) {
				const currentDistance = getDistance(e.touches);
				const zoomFactor = currentDistance / initialDistance;
				scale = Math.min(Math.max(scale * zoomFactor, 1), MAX_SCALE);
				initialDistance = currentDistance;
				updateTransform();
				e.preventDefault();
			} else if (e.touches.length === 1 && isDragging) {
				translateX = e.touches[0].clientX - startX;
				translateY = e.touches[0].clientY - startY;
				updateTransform();
				e.preventDefault();
			}
		});

		container.addEventListener('touchend', () => {
			isDragging = false;
			initialDistance = null;
		});

		imgElement.addEventListener('dragstart', function(e) {
			e.preventDefault();
		});

		container.addEventListener('mousedown', function(e) {
			isDragging = true;
			startX = e.clientX - translateX;
			startY = e.clientY - translateY;
		});

		window.addEventListener('mousemove', function(e) {
			if (isDragging) {
				translateX = e.clientX - startX;
				translateY = e.clientY - startY;
				updateTransform();
			}
		});

		window.addEventListener('mouseup', function() {
			isDragging = false;
		});

		// Handle arrow key panning
		window.addEventListener('keydown', function(e) {
			if (scale <= 1) {
				return; // Only move when zoomed in
			}
			if (e.key === 'ArrowLeft') {
				translateX += MOVE_AMOUNT;
			} else if (e.key === 'ArrowRight') {
				translateX -= MOVE_AMOUNT;
			} else if (e.key === 'ArrowUp') {
				translateY += MOVE_AMOUNT;
			} else if (e.key === 'ArrowDown') {
				translateY -= MOVE_AMOUNT;
			}
			updateTransform();
		});

		const viewer = {
			cleanup
		};

		viewers.set(viewerId, viewer);
	};

	function getViewer(viewerId) {
		console.debug('Getting viewer with id=[', viewerId, ']');
		return viewers.get(viewerId);
	};

	function removeViewer(viewerId) {
		console.debug('Removing viewer with id=[', viewerId, ']');
		viewers.get(viewerId).cleanup();
		viewers.delete(viewerId);
	};

	function clearViewers() {
		console.debug('Removing all viewers');
		viewers.forEach(function (v, k) { v.cleanup(); });
		viewers.clear();
	};

	return {
		initViewer,
		getViewer,
		removeViewer,
		clearViewers
	};
})();