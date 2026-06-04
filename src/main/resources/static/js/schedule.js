 const eventStore = {
     "2026-06-05": [{
             name: "Lunch Meeting",
             date: "June 5, 2026",
             category: "Work"
         },
         {
             name: "Gym Session",
             date: "June 5, 2026",
             category: "Sports"
         }
     ],
     "2026-06-07": [{
             name: "Birthday Party",
             date: "June 7, 2026",
             category: "Party"
         },
         {
             name: "Gift Shopping",
             date: "June 7, 2026",
             category: "Casual"
         },
         {
             name: "Family Dinner",
             date: "June 7, 2026",
             category: "Fine Dining"
         },
         {
             name: "Movie Night",
             date: "June 7, 2026",
             category: "Casual"
         },
         {
             name: "Sleepover Prep",
             date: "June 7, 2026",
             category: "Casual"
         },
         {
             name: "Cake Decoration",
             date: "June 7, 2026",
             category: "Other"
         },
         {
             name: "Game Session",
             date: "June 7, 2026",
             category: "Casual"
         }
     ],
     "2026-06-19": [{
         name: "Team Standup",
         date: "June 19, 2026",
         category: "Work"
     }],
     "2026-06-20": [{
             name: "College Class",
             date: "June 20, 2026",
             category: "College"
         },
         {
             name: "Study Group",
             date: "June 20, 2026",
             category: "College"
         },
         {
             name: "Library Visit",
             date: "June 20, 2026",
             category: "Casual"
         }
     ]
 };

flatpickr("#inputDate", {
    dateFormat: "d-m-Y"
});

let currentSelectedDate = null;

function buildCalendar() {

    const today = new Date();

    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth();

    const monthNames = [
        "JANUARY", "FEBRUARY", "MARCH", "APRIL",
        "MAY", "JUNE", "JULY", "AUGUST",
        "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    ];

    document.getElementById("calendarMonthLabel").innerHTML =
        `${monthNames[currentMonth]} <span class="year-label">${currentYear}</span>`;

    const firstDayOfMonth = new Date(currentYear, currentMonth, 1);
    const lastDayOfMonth = new Date(currentYear, currentMonth + 1, 0);

    let calendarStart = new Date(firstDayOfMonth);

    const startOffset =
        calendarStart.getDay() === 0 ?
        6 :
        calendarStart.getDay() - 1;

    calendarStart.setDate(calendarStart.getDate() - startOffset);

    let calendarEnd = new Date(lastDayOfMonth);

    const endOffset =
        calendarEnd.getDay() === 0 ?
        0 :
        7 - calendarEnd.getDay();

    calendarEnd.setDate(calendarEnd.getDate() + endOffset);

    const totalDays =
        Math.round(
            (calendarEnd - calendarStart) /
            (1000 * 60 * 60 * 24)
        ) + 1;

    const totalRows = Math.ceil(totalDays / 7);

    const grid = document.getElementById("calendarGrid");

    let currentDate = new Date(calendarStart);
    let cellIndex = 0;

    while (currentDate <= calendarEnd) {

        const cell = document.createElement("div");
        cell.className = "day-cell";

        const isInCurrentMonth =
            currentDate.getFullYear() === currentYear &&
            currentDate.getMonth() === currentMonth;

        if (!isInCurrentMonth) {
            cell.classList.add("other-month");
        }

        const currentRowNumber =
            Math.ceil((cellIndex + 1) / 7);

        if (currentRowNumber === totalRows) {
            cell.classList.add("last-row");
        }

        const dayNumberEl = document.createElement("span");
        dayNumberEl.className = "day-number";
        dayNumberEl.textContent =
            buildDayLabel(currentDate, isInCurrentMonth);

        cell.appendChild(dayNumberEl);

        const dateKey = toDateKey(currentDate);
        const dayEvents = eventStore[dateKey];

        if (dayEvents && dayEvents.length > 0) {
            const badge = document.createElement("span");
            badge.className = "event-count-badge";
            badge.textContent = dayEvents.length;
            cell.appendChild(badge);
        }

        const capturedDateKey = dateKey;

        cell.addEventListener("click", function () {
            openEventListModal(capturedDateKey);
        });

        grid.appendChild(cell);

        currentDate.setDate(currentDate.getDate() + 1);
        cellIndex++;
    }
}

 function buildDayLabel(date, isInCurrentMonth) {
     const day = date.getDate();
     // For the boundary months, show "Jan 1" or "Feb 1" on the 1st day
     if (!isInCurrentMonth && day === 1) {
         const shortMonthNames = [
             "Jan", "Feb", "Mar", "Apr", "May", "Jun",
             "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
         ];
         return shortMonthNames[date.getMonth()] + " 1";
     }
     return String(day);
 }

 /* ============================================================
    DATE UTILITIES
 ============================================================ */
 function toDateKey(date) {
     const year = date.getFullYear();
     const month = String(date.getMonth() + 1).padStart(2, "0");
     const day = String(date.getDate()).padStart(2, "0");
     return `${year}-${month}-${day}`;
 }

 function toDisplayDate(dateKey) {
     const parts = dateKey.split("-");
     const year = parseInt(parts[0], 10);
     const month = parseInt(parts[1], 10) - 1;
     const day = parseInt(parts[2], 10);
     const monthNames = [
         "January", "February", "March", "April", "May", "June",
         "July", "August", "September", "October", "November", "December"
     ];
     return `${monthNames[month]} ${day}, ${year}`;
 }

 function openEventListModal(dateKey) {
     currentSelectedDate = dateKey;

     const backdrop = document.getElementById("backdrop");
     const eventListModal = document.getElementById("eventListModal");
     const addEventModal = document.getElementById("addEventModal");

     // Make sure the add event modal is hidden first
     addEventModal.classList.remove("visible");

     // Show backdrop and event list modal
     backdrop.classList.add("visible");
     eventListModal.classList.add("visible");

     // Wire the "Add" button inside the modal to pre-fill with the current date
     document.getElementById("addForDayBtn").onclick = function () {
         openAddEventModal(currentSelectedDate);
     };

     renderEventList(dateKey);
 }

 function renderEventList(dateKey) {
     const eventListBody = document.getElementById("eventListBody");
     eventListBody.innerHTML = "";

     const dayEvents = eventStore[dateKey];

     if (!dayEvents || dayEvents.length === 0) {
         const emptyState = document.createElement("div");
         emptyState.className = "empty-state-box";
         emptyState.textContent = "It seems there are no upcoming events.";
         eventListBody.appendChild(emptyState);
         return;
     }

     dayEvents.forEach(function (event) {
         const card = document.createElement("div");
         card.className = "event-card";
         card.innerHTML = `
          <div class="event-card-image">
            <i class="bi bi-image"></i>
          </div>
          <div class="event-card-info">
            <div class="event-name">${escapeHtml(event.name)}</div>
            <div class="event-date-display">${escapeHtml(event.date)}</div>
            <span class="category-pill">${escapeHtml(event.category)}</span>
          </div>
        `;
         eventListBody.appendChild(card);
     });
 }

 function openAddEventModal(dateKey) {
     const backdrop = document.getElementById("backdrop");
     const eventListModal = document.getElementById("eventListModal");
     const addEventModal = document.getElementById("addEventModal");

     // Swap modals: hide event list, show add event
     eventListModal.classList.remove("visible");
     addEventModal.classList.add("visible");
     backdrop.classList.add("visible");

     // Pre-fill fields
     document.getElementById("inputName").value = "";
     document.getElementById("inputCategory").value = "";
     document.getElementById("inputDate").value = dateKey !== null ? dateKey : "";
 }

 function closeAllModals() {
     document.getElementById("backdrop").classList.remove("visible");
     document.getElementById("eventListModal").classList.remove("visible");
     document.getElementById("addEventModal").classList.remove("visible");
     currentSelectedDate = null;
 }

 /* Pressing ESC also closes all modals */
 document.addEventListener("keydown", function (event) {
     if (event.key === "Escape") {
         closeAllModals();
     }
 });

 function submitAddEvent() {
     const name = document.getElementById("inputName").value.trim();
     const category = document.getElementById("inputCategory").value;
     const dateKey = document.getElementById("inputDate").value;

     if (!name) {
         alert("Please enter an event name.");
         return;
     }
     if (!category) {
         alert("Please choose a category.");
         return;
     }
     if (!dateKey) {
         alert("Please pick a date.");
         return;
     }

     const newEvent = {
         name: name,
         date: toDisplayDate(dateKey),
         category: category
     };

     // Insert into the store
     if (!eventStore[dateKey]) {
         eventStore[dateKey] = [];
     }
     eventStore[dateKey].push(newEvent);

     // Rebuild the calendar so event count badges update
     rebuildCalendarDayCells();

     // Close all modals
     closeAllModals();
 }

 function rebuildCalendarDayCells() {
     const grid = document.getElementById("calendarGrid");
     const allChildren = Array.from(grid.children);

     allChildren.forEach(function (child) {
         if (child.classList.contains("day-cell")) {
             grid.removeChild(child);
         }
     });

     buildCalendar();
 }


 function escapeHtml(rawText) {
     const tempDiv = document.createElement("div");
     tempDiv.appendChild(document.createTextNode(rawText));
     return tempDiv.innerHTML;
 }

 buildCalendar();