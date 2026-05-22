// SPDX-FileCopyrightText: 2026 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport
// SPDX-License-Identifier: EUPL-1.2
// SPDX-FileContributor: Initial development by Curavista

window.MedmijTimeline = (function() {
	function installFeatures() {
		function searchTextInSubtree(node, searchText) {
			if (!node) return false;

			// Check if the current node contains the text
			if (node.nodeType === Node.TEXT_NODE && node.textContent.toLowerCase().includes(searchText.toLowerCase())) {
				return true;
			}

			// Recursively check child nodes
			for (let child of node.childNodes) {
				if (searchTextInSubtree(child, searchText)) {
					return true;
				}
			}

			return false;
		}

		function filterEntries(selectedHcp, selectedFromDate, selectedToDate, searchTitle) {
			console.log('Filter on selectedHcp=' + selectedHcp + ', selectedFromDate=' + selectedFromDate + ', selectedToDate=' + selectedToDate + ', searchTitle=' + searchTitle);
			const filterFromDate = selectedFromDate ? new Date(selectedFromDate) : null;
			const filterToDate = selectedToDate ? new Date(selectedToDate) : null;

			// Step 1: Filter entries
			document.querySelectorAll('.timeline-entry').forEach(entry => {
				const entryHcp = entry.getAttribute('data-timeline-hcp');
				const entryDay = entry.closest('.timeline-day')?.getAttribute('data-timeline-day-value') || '01';
				const entryMonth = entry.closest('.timeline-month')?.getAttribute('data-timeline-month-value') || '01';
				const entryYear = entry.closest('.timeline-year')?.getAttribute('data-timeline-year-value');

				let show = true;
				if (filterFromDate != null || filterToDate != null) {
					if (!entryYear) {
						// Filter entries without date when date filter is active
						show = show && false;
					} else {
						const entryDate = new Date(`${entryYear}-${entryMonth}-${entryDay}`);
						if (filterFromDate != null) {
							// Filter entries that are before filterFromDate
							show = show && filterFromDate <= entryDate;
						}
						if (filterToDate != null) {
							// Filter entries that are after filterToDate
							show = show && entryDate <= filterToDate;
						}
					}
				}
				if (selectedHcp != 'all') {
					// Filter entries that don't match the selected healthcare provider
					show = show && entryHcp == selectedHcp;
				}
				if (searchTitle) {
					show = show && searchTextInSubtree(entry, searchTitle);
				}

				entry.classList.toggle('hidden', !show);
				entry.classList.toggle('visible', show);
			});

			hideParentsWithNoVisibleChildren('.timeline-day', '.timeline-entry.visible');
			hideParentsWithNoVisibleChildren('.timeline-month', '.timeline-day.visible');
			hideParentsWithNoVisibleChildren('.timeline-year', '.timeline-month.visible');
		}

		function hideParentsWithNoVisibleChildren(parentSelector, requiredChildSelector = null) {
		  document.querySelectorAll(parentSelector).forEach(parent => {
			const hidden = parent.querySelector(requiredChildSelector) == null;
			parent.classList.toggle('hidden', hidden);
			parent.classList.toggle('visible', !hidden);
		  });
		}

		function reOrderTimeline(orderClass) {
			const reverseOrderClass = orderClass == 'order-asc' ? 'order-desc' : 'order-asc';
			const timelineData = document.getElementsByClassName("timeline-data")[0];
			[...timelineData.children]
				.reverse()
				.forEach(function(year) {
					if (! timelineData.classList.contains(orderClass)) {
						timelineData.appendChild(year);
					}
					[...year.children]
						.reverse()
						.forEach(function(month) {
							if (! year.classList.contains(orderClass)) {
								year.appendChild(month);
							}
							year.appendChild(month);
							[...month.children]
								.reverse()
								.forEach(function(day) {
									if (! month.classList.contains(orderClass)) {
										month.appendChild(day);
									}
								});
							month.classList.remove(reverseOrderClass);
							month.classList.add(orderClass);
						});
					year.classList.remove(reverseOrderClass);
					year.classList.add(orderClass);
				});
			timelineData.classList.remove(reverseOrderClass);
			timelineData.classList.add(orderClass);
		}

		document.querySelectorAll('.medmij-timeline .timeline-filter').forEach(filter => {
			console.log('Installing timeline features for', filter);
			const hcpEl = filter.querySelector('#medmij-timeline-provider');
			const fromEl = filter.querySelector('#medmij-timeline-from');
			const toEl = filter.querySelector('#medmij-timeline-to');
			const searchTitleEl = filter.querySelector('#medmij-timeline-search');

			filter.querySelectorAll('#medmij-timeline-provider, #medmij-timeline-from, #medmij-timeline-to, #medmij-timeline-searchTitle').forEach(filterEl => {
				filterEl.addEventListener('change', e => {
					filterEntries(hcpEl.value, fromEl.value, toEl.value, searchTitleEl.value);
				});
			});
			searchTitleEl.addEventListener('input', e => {
				filterEntries(hcpEl.value, fromEl.value, toEl.value, searchTitleEl.value);
			});

			const orderEl = filter.querySelector('#medmij-timeline-order');

			orderEl.addEventListener('change', e => {
				reOrderTimeline(e.target.value);
			});
		})
	};

	return {
		installFeatures
	};
})();