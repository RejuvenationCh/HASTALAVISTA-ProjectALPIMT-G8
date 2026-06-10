 // Populated from the backend (GET /api/schedules) by loadSchedulesFromBackend().
 // Shape: { "yyyy-mm-dd": [ { id, name, date, category, targetToken } ] }
 let eventStore = {};

flatpickr("#inputDate", {
    dateFormat: "Y-m-d"
});

/** Loads the user's schedules from the backend and groups them by date. */
async function loadSchedulesFromBackend() {
    try {
        const schedules = await AgendaService.getAgendas();
        eventStore = {};
        schedules.forEach(s => {
            if (!s.eventDate) return;
            const key = s.eventDate; // backend returns ISO yyyy-mm-dd
            if (!eventStore[key]) eventStore[key] = [];
            eventStore[key].push({
                id: s.id,
                name: s.activityName,
                date: toDisplayDate(key),
                category: s.targetTag,
                targetToken: s.targetToken
            });
        });
        rebuildCalendarDayCells();
    } catch (err) {
        console.error('Failed to load schedules', err);
    }
}

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

 /* — Date helpers — */
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

 async function submitAddEvent() {
     const name = document.getElementById("inputName").value.trim();
     const category = document.getElementById("inputCategory").value;
     const dateKey = document.getElementById("inputDate").value;
     const formality = parseInt(document.getElementById("inputFormality").value, 10);

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
     if (!formality || formality < 1 || formality > 5) {
         alert("Please choose a formality level between 1 and 5.");
         return;
     }

     try {
         await AgendaService.addAgenda({
             activityName: name,
             eventDate: dateKey,
             targetToken: formality,
             targetTag: category
         });
         closeAllModals();
         await loadSchedulesFromBackend();
     } catch (err) {
         alert("Could not save event: " + err.message);
     }
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
 loadSchedulesFromBackend();