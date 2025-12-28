(function ($) {
  $(function () {
    const root = document.documentElement;

    // ===============================
    // Theme Toggle
    // ===============================
    function initTheme() {
      const saved = localStorage.getItem("theme") || "light";
      root.setAttribute("data-theme", saved);
    }

    $("#themeToggle").on("click", function () {
      const current = root.getAttribute("data-theme");
      const nextTheme = current === "dark" ? "light" : "dark";
      root.setAttribute("data-theme", nextTheme);
      localStorage.setItem("theme", nextTheme);
    });

    initTheme();

    // ===============================
    // Calendar Events
    // ===============================
    $(".calendar").on("click", ".event", function () {
      alert("لديك حدث: " + $(this).text());
    });

    // ===============================
    // Text Limiter
    // ===============================
    function limitText(selector, maxLength) {
      $(selector).each(function () {
        const text = $.trim($(this).text());
        if (text.length > maxLength) {
          $(this).text(text.substring(0, maxLength) + "…");
        }
      });
    }

    limitText(".limit-100", 100);
    limitText(".limit-150", 150);
    limitText(".limit-250", 250);

    // ===============================
    // Navbar Toggle & Dropdown Logic (INTEGRATED)
    // ===============================

    // 1. Mobile Navbar Toggle
    $(".navbar_toggle").on("click", function () {
      $(".navbar_left").slideToggle(300);
      $(this).toggleClass("open");
    });

    // Close mobile menu when a link is clicked
    $(".navbar__menu li a").on("click", function () {
      if ($(window).width() <= 768) {
        // NOTE: This now ONLY handles closing the menu on mobile.
        // The dropdown logic below will handle opening/closing.
        if (!$(this).parent().hasClass("has-dropdown")) {
          $(".navbar_left").slideUp(300);
          $(".navbar_toggle").removeClass("open");
        }
      }
    });

    // 2. Dropdown and Active Link Management
    $(".navbar__menu li a").on("click", function (e) {
      const parentLi = $(this).parent();

      // If it's a dropdown parent
      if (parentLi.hasClass("has-dropdown")) {
        e.preventDefault(); // prevent navigation

        // Close other dropdowns
        $(".navbar__menu .has-dropdown").not(parentLi).removeClass("open");

        // Toggle current dropdown
        parentLi.toggleClass("open");

        // Clear active from non-dropdown links
        $(".navbar__menu > li > a").removeClass("active");

        // Add active to parent dropdown link
        parentLi.children("a").addClass("active");
      }
      // If it's a child inside dropdown
      else if (parentLi.closest(".has-dropdown").length) {
        // Remove active from normal links
        $(".navbar__menu > li > a").removeClass("active");

        // Keep parent active
        parentLi.closest(".has-dropdown").children("a").addClass("active");
      }
      // Normal non-dropdown links
      else {
        $(".navbar__menu li a").removeClass("active");
        $(this).addClass("active");
      }
    });

    // 3. Close dropdown if clicked outside
    $(document).on("click", function (e) {
      if (!$(e.target).closest(".has-dropdown").length) {
        $(".navbar__menu .has-dropdown").removeClass("open");
      }
    });

    // 4. Navbar Fixed Behavior (Sticky Navbar)
    $(function() {
      var navbar = $(".navbar");
      var originalOffset = navbar.offset().top;
    
      $(window).on("scroll", function () {
        var scrollTop = $(window).scrollTop();
    
        // Add the fixed class when scrolled past original position
        if (scrollTop > originalOffset) {
          if (!navbar.hasClass("fixed")) {
            navbar.addClass("fixed");
          }
        } else {
          navbar.removeClass("fixed");
        }
      });
    });

    // ===============================
    // Password Toggle & Checkbox Logic (NEWLY INTEGRATED)
    // ===============================

    // SVG strings for the eye icon
    const eyeOpen = `
      <svg xmlns="http://www.w3.org/2000/svg" width="22" height="16" viewBox="0 0 22 16" fill="none">
        <path fill-rule="evenodd" clip-rule="evenodd" d="M11 11.75C8.92893 11.75 7.25 10.0711 7.25 8C7.25 5.92893 8.92893 4.25 11 4.25C13.0711 4.25 14.75 5.92893 14.75 8C14.75 10.0711 13.0711 11.75 11 11.75ZM8.75 8C8.75 9.24264 9.75736 10.25 11 10.25C12.2426 10.25 13.25 9.24264 13.25 8C13.25 6.75736 12.2426 5.75 11 5.75C9.75736 5.75 8.75 6.75736 8.75 8Z" fill="#161616"/>
        <path fill-rule="evenodd" clip-rule="evenodd" d="M11 0.25C8.42944 0.25 6.22595 1.38141 4.52031 2.71298C2.81313 4.04576 1.55126 5.61974 0.84541 6.60952L0.792192 6.68373C0.539036 7.03581 0.25 7.43779 0.25 8C0.25 8.56221 0.539036 8.96419 0.792192 9.31627L0.845411 9.39048C1.55126 10.3803 2.81313 11.9542 4.52031 13.287C6.22595 14.6186 8.42944 15.75 11 15.75C13.5706 15.75 15.774 14.6186 17.4797 13.287C19.1869 11.9542 20.4487 10.3803 21.1546 9.39048L21.2078 9.31626C21.461 8.96418 21.75 8.5622 21.75 8C21.75 7.43779 21.461 7.03581 21.2078 6.68373L21.1546 6.60952C20.4487 5.61974 19.1869 4.04576 17.4797 2.71298C15.774 1.38141 13.5706 0.25 11 0.25ZM2.06667 7.48045C2.72687 6.55469 3.89238 5.10618 5.44336 3.89534C6.99587 2.68331 8.88134 1.75 11 1.75C13.1187 1.75 15.0041 2.68331 16.5566 3.89534C18.1076 5.10618 19.2731 6.55469 19.9333 7.48045C20.0967 7.70947 20.1744 7.8213 20.2207 7.91032C20.2502 7.96691 20.2501 7.98242 20.25 7.99761L20.25 8L20.25 8.00239C20.2501 8.01758 20.2502 8.03309 20.2207 8.08968C20.1744 8.1787 20.0967 8.29053 19.9333 8.51955C19.2731 9.44531 18.1076 10.8938 16.5566 12.1047C15.0041 13.3167 13.1187 14.25 11 14.25C8.88134 14.25 6.99588 13.3167 5.44336 12.1047C3.89238 10.8938 2.72687 9.44531 2.06667 8.51955C1.90335 8.29053 1.82562 8.1787 1.77928 8.08968C1.74983 8.03309 1.74991 8.01758 1.74999 8.00239L1.75 8L1.74999 7.99761C1.74991 7.98242 1.74983 7.96691 1.77928 7.91032C1.82562 7.8213 1.90335 7.70947 2.06667 7.48045Z" fill="#161616"/>
      </svg>
    `;

    const eyeClosed = `
      <svg xmlns="http://www.w3.org/2000/svg" width="22" height="16" viewBox="0 0 22 16" fill="none">
        <path d="M1 1L21 15" stroke="#161616" stroke-width="2"/>
        <path d="M11 3C13.5 3 16 5 18 8C16 11 13.5 13 11 13C8.5 13 6 11 4 8C6 5 8.5 3 11 3Z" stroke="#161616" stroke-width="2" fill="none"/>
      </svg>
    `;

      $('.toggle-password').on('click', function() {

          const $toggle = $(this);

          const $wrapper = $toggle.closest('.password-group');


          const $passwordField = $wrapper.find('input[name$="password"]');

          if ($passwordField.length) {

              const type = $passwordField.attr('type') === 'password' ? 'text' : 'password';
              $passwordField.attr('type', type);

              if (type === 'password') {
                  $toggle.html(eyeClosed);
                  $toggle.attr('aria-label', 'show password');
              } else {
                  $toggle.html(eyeOpen);
                  $toggle.attr('aria-label', 'hide password');
              }
          }
      });

    // ===============================
    // Terms Checkbox/Button Toggle Logic
    // ===============================
    const $termsCheck = $("#termsCheck");
    const $acceptBtn = $("#acceptBtn");

    // Check if the required elements for the checkbox toggle exist
    if ($termsCheck.length && $acceptBtn.length) {
      $termsCheck.on("change", function () {
        if ($(this).is(":checked")) {
          $acceptBtn.prop("disabled", false);
        } else {
          $acceptBtn.prop("disabled", true);
        }
      });
    }

    // ===============================
    // Tab Switching Logic (NEWLY ADDED)
    // ===============================
    $(".tab").on("click", function () {
      // Remove 'active' from all tabs
      $(".tab").removeClass("active");
      // Add 'active' to the clicked tab
      $(this).addClass("active");

      // Remove 'active' from all content areas
      $(".content-item").removeClass("active");

      // Get the target selector (e.g., '#content1')
      let target = $(this).data("target");
      // Add 'active' to the matching content area
      // If $(target) is not found, this line does nothing, which is safe.
      $(target).addClass("active");
    });

    // ===============================
    // Custom Select Logic
    // ===============================

    // Open/Close Select Menu
    $(".select-trigger").on("click", function () {
      const $parent = $(this).closest(".custom-select-input");
      // Close all other selects
      $(".custom-select-input").not($parent).removeClass("open");
      // Toggle the clicked one
      $parent.toggleClass("open");
    });

    // Select an Option
    $(".custom-select-input .options li").on("click", function () {
      const $parent = $(this).closest(".custom-select-input");
      // const value = $(this).data("value");
      const text = $(this).text();

      // Update the displayed text
      $parent.find(".select-trigger").text(text);
      // Update 'selected' class
      $parent.find(".options li").removeClass("selected");
      $(this).addClass("selected");
      // Close the dropdown
      $parent.removeClass("open");
    });

    // Close when clicking outside
    $(document).on("click", function (e) {
      // This check handles the 'not found' condition for the select elements
      if (!$(e.target).closest(".custom-select-input").length) {
        $(".custom-select-input").removeClass("open");
      }
    });

    // =====================================
    // Custom Date Picker Logic (INTEGRATED)
    // =====================================

    function generateCalendar($calendar, year, month) {
      $calendar.empty();
      currentYear = year;
      currentMonth = month;

      const date = new Date(year, month, 1);
      const today = new Date();

      // Header
      // NOTE: For RTL support, you might want to swap ◀ and ▶ symbols in your HTML/CSS.
      let html = `
        <div class="calendar-header">
          <button class="prev-month">◀</button>
          <span class="month-label">${date.toLocaleString("default", {
            month: "long",
          })} ${year}</span>
          <button class="next-month">▶</button>
        </div>
      `;

      // Table (Su/Mo/Tu... are hardcoded here, adjust for desired language/order)
      html += "<table><thead><tr>";
      html +=
        "<th>Su</th><th>Mo</th><th>Tu</th><th>We</th><th>Th</th><th>Fr</th><th>Sa</th>";
      html += "</tr></thead><tbody><tr>";

      // Empty cells before first day
      for (let i = 0; i < date.getDay(); i++) {
        html += "<td></td>";
      }

      while (date.getMonth() === month) {
        const day = date.getDate();
        const isToday =
          day === today.getDate() &&
          month === today.getMonth() &&
          year === today.getFullYear();

        // month + 1 is for 1-based month format
        html += `<td class="${isToday ? "today" : ""}" data-date="${year}-${
          month + 1
        }-${day}">${day}</td>`;

        if (date.getDay() === 6) {
          html += "</tr><tr>";
        }

        date.setDate(day + 1);
      }

      html += "</tr></tbody></table>";
      $calendar.html(html);
    }

    // Handle opening the calendar
    // No explicit 'not found' check needed, as jQuery handles the zero-length collection safely.
    $(".date-trigger").on("click", function () {
      const $parent = $(this).closest(".custom-date");
      $(".custom-date").not($parent).removeClass("open");

      const $calendar = $parent.find(".calendar");
      if (!$calendar.children().length) {
        const now = new Date();
        generateCalendar($calendar, now.getFullYear(), now.getMonth());
      }

      $parent.toggleClass("open");
    });

    // Handle date selection
    // Uses event delegation: handles 'not found' because it's attached to the document.
    $(document).on("click", ".calendar td[data-date]", function () {
      const $parent = $(this).closest(".custom-date");
      const date = $(this).data("date");

      $parent.find(".date-trigger").text(date);
        $("#searchDate").val(date);
      $parent.removeClass("open");

      // console.log("Selected date:", date); // Removed console.log
    });

    // Handle prev/next month
    // Uses event delegation: handles 'not found' because it's attached to the document.
    $(document).on("click", ".prev-month", function () {
      const $calendar = $(this).closest(".calendar");
      let year = currentYear;
      let month = currentMonth - 1;
      if (month < 0) {
        month = 11;
        year--;
      }
      generateCalendar($calendar, year, month);
    });

    $(document).on("click", ".next-month", function () {
      const $calendar = $(this).closest(".calendar");
      let year = currentYear;
      let month = currentMonth + 1;
      if (month > 11) {
        month = 0;
        year++;
      }
      generateCalendar($calendar, year, month);
    });

    // Close on outside click (Handles 'not found' safely)
    $(document).on("click", function (e) {
      if (!$(e.target).closest(".custom-date").length) {
        $(".custom-date").removeClass("open");
      }
    });

    $(document).on("click", ".custom-date .calendar", function (e) {
      e.stopPropagation();
    });
    // =====================================
    // End Custom Date Picker Logic

    // ===============================
    // Modal Logic (NEWLY ADDED)
    // ===============================
    const $modal = $("#modal");

    // Only attach events if the modal element exists
    if ($modal.length) {
      // Open modal
      $(".openModal").on("click", function (e) {
        e.preventDefault();
        // console.log($modal); // Removed console.log for cleaner production code
        $modal.css("display", "flex").hide().fadeIn();
      });

      // Close modal when clicking X (using event delegation for robustness)
      // Note: Using the close class might be inside the modal, so we attach to $modal
      $modal.find(".close").on("click", function () {
        $modal.fadeOut();
      });

      // Close modal when clicking outside content (on the window click event)
      $(window).on("click", function (e) {
        // Check if the clicked element is the modal backdrop itself
        if ($(e.target).is($modal)) {
          $modal.fadeOut();
        }
      });
    }


    // ===================================
    // FullCalendar Initialization (NEWLY INTEGRATED)
    // ===================================
    // NOTE: This code block replaces the outer 'document.addEventListener'
    // since the whole function is already wrapped in a jQuery DOM ready handler.

   

    // ===================================
    // Splide Banner Slider Initialization (NEWLY INTEGRATED)
    // ===================================
    const bannerSliderEl = document.getElementById("banner-slider");

    // Check if the element exists and Splide is loaded
    if (bannerSliderEl && typeof Splide !== "undefined") {
      const html = document.documentElement;
      let isRTL = html.getAttribute("dir") === "rtl";
      new Splide("#banner-slider", {
        type: "",
        perPage: 1,
        autoplay: true,
        interval: 3000,
        perMove: 1,
        // Reusing the isRTL variable from the parent scope
        direction: isRTL ? "rtl" : "ltr",
        arrows: true,
        pagination: false,
      }).mount();
    }
    // ===================================
    // End Splide Banner Slider Initialization
    // ===================================
    // End Splide Code

    // ===============================
    // Liferay Asset Publisher Fix
    // ===============================

    // ===================================
    // Splide Announcements Initialization (NEWLY INTEGRATED)
    // ===================================
    const announcementsEl = document.getElementById("announcements");

    if (announcementsEl && typeof Splide !== "undefined") {
      const html = document.documentElement;
      let isRTL = html.getAttribute("dir") === "rtl";

      new Splide("#announcements", {
        // Note: The original 'type' was empty, defaulting to 'slide'.
        // Keeping it empty or setting it to 'slide' is fine.
        type: "",
        perPage: 4,
        autoplay: true,
        interval: 5000,
        perMove: 1,
        direction: isRTL ? "rtl" : "ltr",
        arrows: false,
        pagination: false,

        breakpoints: {
          1280: {
            perPage: 4,
          },
          1024: {
            perPage: 4,
          },
          768: {
            perPage: 2,
          },
          576: {
            perPage: 1,
          },
        },
      }).mount();
    }
    // ===================================
    // End Splide Announcements Initialization

    // ===================================
    // Splide Quick Links Initialization (NEWLY INTEGRATED)
    // ===================================
    const quickLinksEl = document.getElementById("quick-links");

    if (quickLinksEl && typeof Splide !== "undefined") {
      const html = document.documentElement;
      let isRTL = html.getAttribute("dir") === "rtl";

      const splide = new Splide("#quick-links", {
        type: "loop",
        height: "14rem",
        perPage: 1,
        perMove: 1,
        arrows: false,
        // Grid options rely on the Splide Grid Extension
        grid: {
          rows: 2,
          cols: 3,
          gap: {
            row: "2px",
            col: "2px",
          },
        },
        breakpoints: {
          640: {
            height: "8rem",
            perPage: 1,
            grid: {
              rows: 2,
              cols: 2,
              gap: { row: "2px", col: "2px" },
            },
          },
        },
        // The isRTL variable is still in scope from the parent function
        direction: isRTL ? "rtl" : "ltr",
      });

      // Check for the Grid extension's existence on the window.splide object
      // before attempting to mount it, ensuring robustness.
      splide.mount(
        window.splide?.Extensions ? { Grid: window.splide.Extensions.Grid } : {}
      );
    }
    // ===================================
    // End Splide Quick Links Initialization

    (function () {
      const portlets = document.querySelectorAll(
        "[id^='com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_']"
      );

      portlets.forEach(function (portlet) {
        const fnName =
          "_" + portlet.id.replace(/-/g, "_") + "_handleDropdownKeyPress";

        if (typeof window[fnName] !== "function") {
          window[fnName] = function () {
            // no-op to prevent console error
            return true;
          };
        }
      });
    })();
  });
})(jQuery);
/* Search   */
// Wait for the document to be fully loaded
document.addEventListener('DOMContentLoaded', function() {

    const searchForm = document.getElementById('navbarSearchForm');
    const searchInput = searchForm.querySelector('.search-input');
    const searchButton = searchForm.querySelector('.search-button');
    const searchUrl = searchForm.action; // Gets '/en/search'


    function handleSearchSubmit() {
        const query = searchInput.value.trim();

        if (query) {
            window.location.href = searchUrl + '?q=' + encodeURIComponent(query);
        } else {
            searchForm.classList.remove('active');
        }
    }

    // --- 1. Handle the click on the search icon ---
    searchButton.addEventListener('click', function(event) {
        // Check if the form is already active
        const isActive = searchForm.classList.contains('active');

        if (!isActive) {
            event.preventDefault();
            searchForm.classList.add('active');
            searchInput.focus();
        }else{
            handleSearchSubmit();
        }
    });

    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();

        handleSearchSubmit();
    });

    document.addEventListener('click', function(event) {
        if (!searchForm.contains(event.target) && searchForm.classList.contains('active')) {
            searchForm.classList.remove('active');
        }
    });
});

/* Human of X */
    $(document).ready(function(){
       setTimeout(function() {
      $('.trim40').each(function(){
        var $this = $(this);
        var fullText = $.trim($this.text());
        var limit = parseInt($this.attr('data-trim')) || 45;

        if(fullText.length > limit){
          var trimmed = fullText.substring(0, limit) + '...';
          $this.text(trimmed);
          $this.attr('title', fullText);
        }
      });
      }, 500);
    });
    $(document).ready(function(){
      setTimeout(function() {
      $('.trim100').each(function(){
        var $this = $(this);
        var fullText = $.trim($this.text());
        var limit = parseInt($this.attr('data-trim')) || 100;

        if(fullText.length > limit){
          var trimmed = fullText.substring(0, limit) + '...';
          $this.text(trimmed);
          $this.attr('title', fullText);
        }
      });
      }, 500);
    });
    $(document).ready(function(){
      setTimeout(function() {
      $('.notification-box p, .trim400').each(function(){
        var $this = $(this);
        var fullText = $.trim($this.text());
        var limit = parseInt($this.attr('data-trim')) || 400;

        if(fullText.length > limit){
          var trimmed = fullText.substring(0, limit) + '...';
          $this.text(trimmed);
          $this.attr('title', fullText);
        }
      });
      }, 500);
    });

    (function(){
      const newCard = document.querySelectorAll('.newCard');
      const humanxmodal = document.getElementById('humanxmodal');
      const player = document.getElementById('humanxplayer');
      const mTitle = document.getElementById('humanxmTitle');
      const mDesc = document.getElementById('humanxmDesc');
      const backClose = document.getElementById('humanxbackClose');

      function openModal(card){
        const src = card.dataset.src;
        const title = card.dataset.title || '';
        const desc = card.dataset.desc || '';
        const poster = card.dataset.poster || '';
        player.src = src;
        player.poster = poster;
        player.currentTime = 0;
        mTitle.textContent = title;
        mDesc.textContent = desc;
        humanxmodal.style.display = 'flex';
        setTimeout(()=>{ player.play().catch(()=>{}); }, 150);
      }

      function closeModal(){
        player.pause();
        player.removeAttribute('src');
        player.load();
        humanxmodal.style.display = 'none';
      }

      newCard.forEach(card=>{
        const btn = card.querySelector('.play-btn');
        btn.addEventListener('click', ()=> openModal(card));
        const img = card.querySelector('img');
        img.addEventListener('click', ()=> openModal(card));
      });

      backClose.addEventListener('click', closeModal);
      humanxmodal.addEventListener('click', (e)=>{ if(e.target === humanxmodal) closeModal(); });
      document.addEventListener('keydown', (e)=>{ if(e.key === 'Escape') closeModal(); });

    })();
  /* End of Human of X*/
    $(document).ready(function() {
    if ($('.splide__list li').length === 1) {
        $('.splide__arrow').hide();
    }
});