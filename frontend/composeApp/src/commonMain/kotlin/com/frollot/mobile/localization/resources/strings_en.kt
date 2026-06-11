package com.frollot.mobile.localization.resources

/**
 * Bundle of English strings.
 * 
 * Complete translations for all application screens and components.
 */
fun createEnglishStrings(): StringsBundle {
    val strings = mapOf<String, String>(
        // ========================================
        // LOGIN SCREEN
        // ========================================
        "login.welcome_title" to "Welcome 👋",
        "login.welcome_subtitle" to "Sign in and explore\nendless possibilities",
        "login.email_label" to "Email address",
        "login.email_placeholder" to "you@example.com",
        "login.password_label" to "Password",
        "login.password_placeholder" to "••••••••",
        "login.forgot_password" to "Forgot password?",
        "login.submit_button" to "Sign in",
        "login.submit_button_loading" to "Signing in...",
        "login.continue_with" to "OR CONTINUE WITH",
        "login.google_button" to "Google",
        "login.facebook_button" to "Facebook",
        "login.new_here" to "New here?",
        "login.create_account" to "Create an account",
        "login.secure_connection" to "100% secure connection",
        
        // Login error messages
        "login.errors.invalid_credentials" to "Incorrect email or password",
        "login.errors.account_disabled" to "Account disabled. Please contact support.",
        "login.errors.account_not_found" to "Account not found",
        "login.errors.server_unavailable" to "Unable to contact server",
        "login.errors.timeout" to "Request timeout. Check your internet connection.",
        "login.errors.generic_error" to "Error signing in",
        
        // ========================================
        // REGISTER SCREEN
        // ========================================
        "register.welcome_title" to "Join us 🎉",
        "register.welcome_subtitle" to "Create your account in seconds",
        "register.first_name_label" to "First name",
        "register.first_name_placeholder" to "Sophie",
        "register.last_name_label" to "Last name",
        "register.last_name_placeholder" to "Martin",
        "register.email_label" to "Email address",
        "register.email_placeholder" to "you@example.com",
        "register.account_type_label" to "Account type",
        "register.password_label" to "Password",
        "register.password_placeholder" to "Minimum 8 characters",
        "register.confirm_password_label" to "Confirm password",
        "register.confirm_password_placeholder" to "Re-enter your password",
        "register.password_mismatch" to "Passwords do not match",
        "register.submit_button" to "Create my account",
        "register.submit_button_loading" to "Creating account...",
        "register.sign_up_with" to "OR SIGN UP WITH",
        "register.google_button" to "Google",
        "register.facebook_button" to "Facebook",
        "register.already_registered" to "Already registered?",
        "register.login_link" to "Sign in",
        "register.data_protected" to "Your data is protected",
        "register.success_message" to "Welcome {firstName} {lastName}! Your account has been created successfully.",
        
        // Account types
        "register.user_types.client" to "👤 Client",
        "register.user_types.hairstylist" to "✂️ Hairstylist",
        "register.user_types.salon_owner" to "🏢 Salon owner",
        "register.user_types.admin" to "⚙️ Administrator",
        
        // Registration error messages
        "register.errors.email_already_used" to "This email is already in use",
        "register.errors.invalid_data" to "Invalid data, please check your information",
        "register.errors.server_unavailable" to "Unable to contact server",
        "register.errors.timeout" to "Request timeout. Check your internet connection.",
        "register.errors.generic_error" to "Error registering",
        
        // ========================================
        // HOME SCREEN
        // ========================================
        "home.title" to "Frollot",
        "home.filters_button" to "Filters",
        "home.my_bookings_button" to "My bookings",
        "home.create_salon_button" to "Create a salon",
        "home.loading_message" to "Loading salons",
        "home.error_message" to "Unable to load salons",
        "home.empty_state_title" to "No salons available",
        "home.empty_state_title_with_filters" to "No salons match your criteria",
        "home.empty_state_message" to "Be the first to create a salon!",
        "home.empty_state_message_with_filters" to "Try broadening your search or removing some filters.",
        "home.create_my_salon" to "Create my salon",
        "home.premium_salons" to "Premium Salons",
        "home.near_me" to "Near me",
        "home.city" to "City",
        "home.salons_found" to "{count} salon found",
        "home.salons_found_plural" to "{count} salons found",
        "home.filter_by_city" to "Filter by city",
        "home.filter_by_city_description" to "Enter a city to filter salons.",
        "home.city_placeholder" to "Ex: Paris",
        "home.apply" to "Apply",
        "home.reset" to "Reset",
        "home.premium_badge" to "PREMIUM",
        "home.rating" to "Rating",
        "home.open_hours" to "Open • 9am-7pm",
        "home.cut" to "Cut",
        "home.coloring" to "Coloring",
        "home.book" to "Book",
        "home.cover_photo_description" to "Cover photo of {salonName}",
        
        // ========================================
        // SETTINGS SCREEN
        // ========================================
        "settings.title" to "Settings",
        
        // Sections
        "settings.sections.account" to "Account",
        "settings.sections.privacy" to "Privacy",
        "settings.sections.notifications" to "Notifications",
        "settings.sections.appearance" to "Appearance",
        "settings.sections.social_network" to "Social network",
        "settings.sections.bookings" to "Bookings",
        "settings.sections.content_and_media" to "Content & Media",
        "settings.sections.data_privacy" to "Data Privacy",
        "settings.sections.help_and_support" to "Help & Support",
        "settings.sections.about" to "About",
        
        // Account
        "settings.account.profile" to "Profile",
        "settings.account.profile_subtitle" to "Manage your profile and information",
        "settings.account.security" to "Security",
        "settings.account.security_subtitle" to "Password, two-factor authentication",
        "settings.account.email" to "Email",
        "settings.account.phone" to "Phone",
        "settings.account.phone_subtitle" to "Add a phone number",
        "settings.account.not_defined" to "Not defined",
        
        // Privacy
        "settings.privacy.profile_visibility" to "Profile visibility",
        "settings.privacy.who_can_follow_me" to "Who can follow me",
        "settings.privacy.who_can_message_me" to "Who can message me",
        "settings.privacy.show_activity_status" to "Show activity status",
        "settings.privacy.show_activity_status_subtitle" to "Other users will see when you're online",
        "settings.privacy.blocked_users" to "Blocked users",
        "settings.privacy.blocked_users_count" to "{count} user(s)",
        "settings.privacy.no_blocked_users" to "No blocked users",
        "settings.privacy.options.public" to "Public",
        "settings.privacy.options.followers_only" to "Followers only",
        "settings.privacy.options.private" to "Private",
        "settings.privacy.options.everyone" to "Everyone",
        "settings.privacy.options.nobody" to "Nobody",
        
        // Notifications
        "settings.notifications.title" to "Notifications",
        "settings.notifications.subtitle" to "Enable or disable all notifications",
        "settings.notifications.push" to "Push notifications",
        "settings.notifications.push_subtitle" to "Receive notifications on your device",
        "settings.notifications.email" to "Email notifications",
        "settings.notifications.email_subtitle" to "Receive notifications by email",
        "settings.notifications.bookings" to "Bookings",
        "settings.notifications.bookings_subtitle" to "Notifications for your appointments",
        "settings.notifications.social" to "Social network",
        "settings.notifications.social_subtitle" to "Likes, comments, mentions, etc.",
        "settings.notifications.marketing" to "Marketing",
        "settings.notifications.marketing_subtitle" to "Special offers and news",
        
        // Appearance
        "settings.appearance.dark_mode" to "Dark mode",
        "settings.appearance.dark_mode_subtitle" to "Enable dark theme",
        "settings.appearance.language" to "Language",
        
        // Social network
        "settings.social_network.post_visibility_default" to "Default post visibility",
        "settings.social_network.allow_comments" to "Allow comments",
        "settings.social_network.allow_comments_subtitle" to "Other users can comment on your posts",
        "settings.social_network.allow_reactions" to "Allow reactions",
        "settings.social_network.allow_reactions_subtitle" to "Other users can react to your posts",
        "settings.social_network.allow_shares" to "Allow sharing",
        "settings.social_network.allow_shares_subtitle" to "Other users can share your posts",
        
        // Bookings
        "settings.bookings.booking_notifications" to "Booking notifications",
        "settings.bookings.booking_notifications_subtitle" to "Receive notifications for your appointments",
        "settings.bookings.availability_preferences" to "Availability preferences",
        "settings.bookings.availability_preferences_subtitle" to "Manage your preferred time slots",
        "settings.bookings.payment_methods" to "Payment methods",
        "settings.bookings.payment_methods_subtitle" to "Manage your payment methods",
        
        // Content and media
        "settings.content_and_media.auto_save_photos" to "Auto-save photos",
        "settings.content_and_media.auto_save_photos_subtitle" to "Save photos to your gallery",
        "settings.content_and_media.data_usage" to "Data usage",
        "settings.content_and_media.video_quality" to "Video quality",
        "settings.content_and_media.data_usage_options.economical" to "Economical",
        "settings.content_and_media.data_usage_options.standard" to "Standard",
        "settings.content_and_media.data_usage_options.high" to "High",
        "settings.content_and_media.video_quality_options.sd" to "SD",
        "settings.content_and_media.video_quality_options.hd" to "HD",
        "settings.content_and_media.video_quality_options.full_hd" to "Full HD",
        
        // Data privacy
        "settings.data_privacy.download_data" to "Download your data",
        "settings.data_privacy.download_data_subtitle" to "Get a copy of your data",
        "settings.data_privacy.delete_account" to "Delete your account",
        "settings.data_privacy.delete_account_subtitle" to "Permanently delete your account and all your data",
        
        // Help and support
        "settings.help_and_support.help_center" to "Help center",
        "settings.help_and_support.help_center_subtitle" to "FAQ and usage guides",
        "settings.help_and_support.contact_us" to "Contact us",
        "settings.help_and_support.contact_us_subtitle" to "Report an issue or ask a question",
        "settings.help_and_support.report_bug" to "Report a bug",
        "settings.help_and_support.report_bug_subtitle" to "Help us improve the app",
        "settings.help_and_support.rate_app" to "Rate the app",
        "settings.help_and_support.rate_app_subtitle" to "Share your feedback on the store",
        
        // About
        "settings.about.version" to "Version",
        "settings.about.terms_of_service" to "Terms of service",
        "settings.about.terms_of_service_subtitle" to "Read the terms of service",
        "settings.about.privacy_policy" to "Privacy policy",
        "settings.about.privacy_policy_subtitle" to "Read the privacy policy",
        "settings.about.licenses" to "Licenses",
        "settings.about.licenses_subtitle" to "Open source licenses",
        
        // Actions
        "settings.actions.logout" to "Logout",
        "settings.actions.logout_dialog_title" to "Logout",
        "settings.actions.logout_dialog_text" to "Are you sure you want to log out?",
        "settings.actions.delete" to "Delete",
        "settings.actions.delete_account_dialog_title" to "Delete your account",
        "settings.actions.delete_account_dialog_text" to "This action is irreversible. All your data will be permanently deleted. Are you absolutely sure?",
        "settings.actions.cancel" to "Cancel",
        
        // ========================================
        // PROFILE SCREEN
        // ========================================
        "profile.version" to "Version 1.0.0",
        "profile.logout_dialog_title" to "Logout",
        "profile.logout_dialog_text" to "Are you sure you want to log out?",
        "profile.logout_confirm" to "Yes, log me out",
        "profile.cancel" to "Cancel",
        "profile.avatar_preview" to "Avatar preview",
        "profile.save" to "Save",
        "profile.edit_photo" to "Edit photo",
        "profile.account_verified" to "Verified account",
        "profile.account_not_verified" to "Not verified",
        "profile.account_info" to "Account information",
        "profile.email" to "Email",
        "profile.phone" to "Phone",
        "profile.member_since" to "Member since",
        "profile.account_type" to "Account type",
        "profile.edit_profile" to "Edit my profile",
        "profile.edit_profile_subtitle" to "Change name, first name, phone",
        "profile.change_password" to "Change password",
        "profile.change_password_subtitle" to "Update your credentials",
        "profile.logout" to "Log out",
        "profile.logout_subtitle" to "End your session",
        "profile.statistics" to "Statistics",
        "profile.salons" to "Salons",
        "profile.bookings" to "Bookings",
        "profile.reviews" to "Reviews",
        "profile.services" to "Services",
        "profile.points" to "Points",
        "profile.user_types.client" to "Client",
        "profile.user_types.salon_owner" to "Salon owner",
        "profile.user_types.hairstylist" to "Hairstylist",
        "profile.user_types.admin" to "Administrator",
        
        // ========================================
        // SOCIAL FEED SCREEN
        // ========================================
        "social_feed.filter_by_type" to "Filter by type",
        "social_feed.my_follows" to "My follows",
        "social_feed.near_me" to "Near me",
        "social_feed.all" to "All",
        "social_feed.add_comment" to "Add a comment (optional)",
        "social_feed.share" to "Share",
        "social_feed.cancel" to "Cancel",
        "social_feed.no_collection_yet" to "You don't have any collection yet",
        "social_feed.save" to "Save",
        "social_feed.archive" to "Archive",
        "social_feed.share_in_app" to "Share in app",
        "social_feed.share_external" to "Share externally",
        "social_feed.report" to "Report",
        "social_feed.add_to_collection_example" to "E.g.: I booked at this salon thanks to this post!",
        "social_feed.add_to_collection" to "Add to collection",
        "social_feed.create_collection" to "Create collection",
        
        // ========================================
        // MY BOOKINGS SCREEN
        // ========================================
        "my_bookings.service" to "Service",
        "my_bookings.date_time" to "Date & Time",
        "my_bookings.hairstylist" to "Hairstylist",
        "my_bookings.amount" to "Amount",
        "my_bookings.review_left" to "Review left",
        "my_bookings.leave_review" to "Leave a review",
        "my_bookings.cancel" to "Cancel",
        "my_bookings.cancel_confirm" to "Yes, cancel",
        "my_bookings.cancel_keep" to "No, keep",
        
        // ========================================
        // BOOKING SCREEN (WIZARD)
        // ========================================
        "booking.back_to_home" to "Back to home",
        "booking.duration" to "Duration",
        "booking.category" to "Category",
        "booking.choose_expert" to "Choose your expert",
        "booking.choose_expert_subtitle" to "Select a hairstylist or let the salon choose",
        "booking.select_date" to "Select a date",
        "booking.select_date_subtitle" to "Choose your appointment day",
        "booking.choose_time_slot" to "Choose a time slot",
        "booking.choose_time_slot_subtitle" to "Select the time that suits you",
        "booking.notes_or_special_requests" to "Notes or special requests",
        "booking.notes_or_special_requests_subtitle" to "Optional - Let us know your preferences",
        "booking.notes_info" to "This information will be passed on to your hairstylist",
        "booking.expert_selected" to "Expert selected",
        "booking.continue" to "Continue",
        "booking.time_slot_selected" to "Time slot selected",
        "booking.pay_now" to "Pay now",
        "booking.view_my_bookings" to "View my bookings",
        
        // ========================================
        // BOOKING DETAIL SCREEN
        // ========================================
        "booking_detail.cancel_keep" to "No, keep",
        "booking_detail.hair_salon" to "Hair Salon",
        "booking_detail.click_for_details" to "Click to see details",
        "booking_detail.service" to "Service",
        "booking_detail.service_details" to "Service details",
        "booking_detail.booking" to "Booking",
        "booking_detail.booking_details" to "Date, time and hairstylist",
        "booking_detail.payment" to "Payment",
        "booking_detail.payment_details" to "Transaction details",
        "booking_detail.amount" to "AMOUNT",
        "booking_detail.cancel_booking_title" to "Cancel booking?",
        "booking_detail.cancel_confirm_message" to "Are you sure you want to cancel this booking? This action is irreversible.",
        "booking_detail.booking_status" to "Your booking status",
        
        // ========================================
        // SALON DETAIL SCREEN
        // ========================================
        "salon_detail.services" to "Services",
        "salon_detail.view_posts" to "View posts",
        "salon_detail.open_feed" to "Open feed",
        "salon_detail.add_member" to "Add a member",
        "salon_detail.queue" to "Queue",
        "salon_detail.position" to "Position: #{position}",
        "salon_detail.leave_queue" to "Leave queue",
        "salon_detail.join_queue" to "Join queue",
        "salon_detail.login_to_join_queue" to "Sign in to join the queue",
        "salon_detail.cancel" to "Cancel",
        "salon_detail.subscribers" to "Subscribers",
        "salon_detail.book" to "Book",
        "salon_detail.salon_verified" to "Verified salon",
        "salon_detail.clients" to "Clients",
        "salon_detail.waiting" to "Waiting",
        "salon_detail.your_position" to "Your position",
        "salon_detail.position_in_queue" to "Number {position} in queue",
        "salon_detail.my_team" to "My Team",
        "salon_detail.team" to "Team",
        "salon_detail.our_services" to "Our Services",
        "salon_detail.choose_from_services" to "Choose from {count} service",
        "salon_detail.choose_from_services_plural" to "Choose from {count} services",
        "salon_detail.loading_services" to "Loading services",
        "salon_detail.please_wait" to "Please wait...",
        "salon_detail.loading_error" to "Loading error",
        "salon_detail.no_services_available" to "No services available",
        "salon_detail.no_services_message" to "This salon hasn't added any services yet.\nCome back later!",
        "salon_detail.ready_to_book" to "Ready to book?",
        "salon_detail.reviews_and_ratings" to "Reviews & Ratings",
        
        // ========================================
        // CREATE SALON SCREEN
        // ========================================
        "create_salon.launch_your_salon" to "Launch your salon",
        "create_salon.join_community" to "Join our community of professionals",
        "create_salon.salon_info" to "Salon information",
        "create_salon.cover_photo" to "Cover photo",
        "create_salon.cover_photo_hint" to "Optional • JPG, PNG • Max 10MB",
        "create_salon.add_photo" to "Add a photo",
        "create_salon.click_to_browse" to "Click to browse your files",
        "create_salon.data_secured" to "Your data is secured and protected",
        
        // ========================================
        // POST DETAIL SCREEN
        // ========================================
        "post_detail.comments" to "{count} comment",
        "post_detail.comments_plural" to "{count} comments",
        
        // ========================================
        // CREATE POST SCREEN
        // ========================================
        "create_post.public" to "Public",
        "create_post.post_type" to "Post type",
        "create_post.visibility" to "Visibility",
        "create_post.at_least_two_images" to "At least 2 images (Before + After)",
        "create_post.click_to_select_photo" to "Click to select a photo",
        "create_post.photo_format_hint" to "JPG, PNG (max 10MB)",
        "create_post.post_will_be_visible" to "Your post will be visible to all users",
        
        // ========================================
        // COIFFEUR PROFILE SCREEN
        // ========================================
        "coiffeur_profile.pinned_posts" to "Pinned posts",
        "coiffeur_profile.recent_posts" to "Recent posts",
        "coiffeur_profile.unfollow" to "Unfollow",
        "coiffeur_profile.follow" to "Follow",
        "coiffeur_profile.badges_and_certifications" to "Badges and Certifications",
        "coiffeur_profile.featured_portfolio" to "Featured portfolio",
        "coiffeur_profile.portfolios" to "Portfolios",
        
        // ========================================
        // SALON SOCIAL PROFILE SCREEN
        // ========================================
        "salon_social_profile.recent_posts" to "Recent posts",
        "salon_social_profile.unfollow" to "Unfollow",
        "salon_social_profile.follow" to "Follow",
        "salon_social_profile.featured_posts" to "Featured posts",
        "salon_social_profile.portfolios" to "Portfolios",
        "salon_social_profile.verified" to "Verified",
        "salon_social_profile.services" to "Services",
        
        // ========================================
        // CLIENT PROFILE SCREEN
        // ========================================
        "client_profile.title" to "Client Profile",
        "client_profile.about" to "About",
        "client_profile.posts" to "Posts",
        "client_profile.likes" to "Likes",
        "client_profile.followers" to "Followers",
        "client_profile.following" to "Following",
        "client_profile.collections" to "Collections",
        "client_profile.collections_count" to "Collections",
        "client_profile.recent_posts" to "Recent Posts",
        "client_profile.badges" to "Badges",
        "client_profile.follow" to "Follow",
        "client_profile.unfollow" to "Unfollow",
        
        // ========================================
        // SALON OWNER PROFILE SCREEN
        // ========================================
        "salon_owner_profile.title" to "Salon Owner Profile",
        "salon_owner_profile.about" to "About",
        "salon_owner_profile.posts" to "Posts",
        "salon_owner_profile.likes" to "Likes",
        "salon_owner_profile.followers" to "Followers",
        "salon_owner_profile.salons" to "Salons",
        "salon_owner_profile.collections" to "Collections",
        "salon_owner_profile.collections_count" to "Collections",
        "salon_owner_profile.recent_posts" to "Recent Posts",
        "salon_owner_profile.badges" to "Badges",
        "salon_owner_profile.follow" to "Follow",
        "salon_owner_profile.unfollow" to "Unfollow",
        
        // ========================================
        // COMMENTS SCREEN
        // ========================================
        "comments.title" to "Comments",
        "comments.add_comment" to "Add a comment...",
        "comments.load_more" to "Load more comments",
        
        // ========================================
        // COLLECTIONS SCREEN
        // ========================================
        "collections.title" to "Collections",
        "collections.post" to "post",
        "collections.posts" to "posts",
        "collections.edit" to "Edit",
        "collections.delete" to "Delete",
        "collections.new_collection" to "New collection",
        "collections.name" to "Name *",
        "collections.description" to "Description (optional)",
        "collections.category" to "Category",
        "collections.public_collection" to "Public collection",
        "collections.save_to_collection" to "Save to collection",
        "collections.no_collections" to "No collections yet. Create one first!",
        
        // ========================================
        // COLLECTION DETAIL SCREEN
        // ========================================
        "collection_detail.title" to "Collection",
        "collection_detail.load_more" to "Load more",
        "collection_detail.edit" to "Edit",
        "collection_detail.delete" to "Delete",
        "collection_detail.delete_collection" to "Delete collection",
        "collection_detail.delete_collection_message" to "Are you sure you want to delete this collection? All posts will be removed from the collection.",
        "collection_detail.remove_from_collection" to "Remove from collection",
        
        // ========================================
        // CREATE PORTFOLIO SCREEN
        // ========================================
        "create_portfolio.title" to "Create a portfolio",
        "create_portfolio.no_salon_found" to "No salon found. Please create a salon first.",
        "create_portfolio.salon" to "Salon",
        "create_portfolio.name" to "Portfolio name *",
        "create_portfolio.name_placeholder" to "E.g.: My Colorings 2024",
        "create_portfolio.description" to "Description",
        "create_portfolio.cover_image" to "Cover image",
        "create_portfolio.add_cover_image" to "Add a cover image",
        "create_portfolio.public_portfolio" to "Public portfolio",
        
        // ========================================
        // PORTFOLIO DETAIL SCREEN
        // ========================================
        "portfolio_detail.load_more" to "Load more",
        
        // ========================================
        // PORTFOLIOS LIST SCREEN
        // ========================================
        "portfolios_list.create_portfolio" to "Create a portfolio",
        
        // ========================================
        // CREATE SERVICE SCREEN
        // ========================================
        "create_service.premium_service" to "Premium service",
        "create_service.service_info" to "Service information",
        "create_service.category" to "Category *",
        "create_service.price_adjustment_info" to "Prices can be adjusted at any time from your professional space.",
        "create_service.name" to "Service name *",
        "create_service.name_placeholder" to "E.g.: Men's Cut, Full Color...",
        "create_service.description" to "Description",
        "create_service.duration" to "Duration (min) *",
        "create_service.price" to "Price (€) *",
        
        // ========================================
        // SALON POSTS SCREEN
        // ========================================
        "salon_posts.title" to "Posts from {salonName}",
        "salon_posts.all" to "All",
        "salon_posts.all_services" to "All services",
        "salon_posts.popular" to "Popular",
        
        // ========================================
        // ARCHIVES SCREEN
        // ========================================
        "archives.title" to "My Archives",
        
        // ========================================
        // FAVORITES SCREEN
        // ========================================
        "favorites.title" to "My Favorites",
        "favorites.offline_mode" to "Offline mode - Cached data",
        
        // ========================================
        // TRENDING SCREEN
        // ========================================
        "trending.title" to "Trending",
        "trending.posts" to "Posts",
        "trending.hashtags" to "Hashtags",
        "trending.salons" to "Salons",
        
        // ========================================
        // REPORT SCREEN
        // ========================================
        "report.info_message" to "Your report will be reviewed by our moderation team. Thank you for helping us maintain a respectful community.",
        "report.reason_title" to "Report reason *",
        "report.additional_info" to "Additional information (optional)",
        "report.cancel" to "Cancel",
        "report.submit" to "Report",
        "report.reported_content" to "Reported content",
        "report.post_author" to "Post author",
        
        // ========================================
        // CREATE STAFF SCREEN
        // ========================================
        "create_staff.title" to "Add a member",
        "create_staff.new_collaborator" to "New collaborator",
        "create_staff.personal_info" to "Personal information",
        "create_staff.specialties" to "Specialties",
        "create_staff.specialties_hint" to "Select the service categories this member can perform.",
        "create_staff.email_info" to "The collaborator will receive an email with their login information.",
        "create_staff.first_name" to "First name *",
        "create_staff.first_name_placeholder" to "E.g.: John",
        "create_staff.last_name" to "Last name *",
        "create_staff.last_name_placeholder" to "E.g.: Smith",
        "create_staff.email" to "Email *",
        
        // ========================================
        // QUEUE MANAGEMENT SCREEN
        // ========================================
        "queue_management.title" to "{salonName} - Queue Management",
        "queue_management.remove" to "Remove",
        "queue_management.ticket" to "Ticket #{position}",
        "queue_management.arrived" to "Arrived",
        "queue_management.estimated_wait" to "Estimated wait",
        "queue_management.call_next" to "Call next",
        
        // ========================================
        // CHANGE EMAIL SCREEN
        // ========================================
        "change_email.title" to "Change Email",
        "change_email.current_email" to "Current Email",
        "change_email.new_email_section" to "New Email",
        "change_email.new_email" to "New Email",
        "change_email.confirm_with_password" to "Confirm with your password",
        "change_email.save" to "Save",
        "change_email.saving" to "Saving...",
        "change_email.all_fields_required" to "Please fill in all fields",
        "change_email.invalid_email" to "Invalid email address",
        "change_email.error" to "Error changing email",
        "change_email.info" to "Your new email will be used to log into your account.",
        
        // ========================================
        // CHANGE PHONE SCREEN
        // ========================================
        "change_phone.title" to "Change Phone",
        "change_phone.current_phone" to "Current Phone",
        "change_phone.not_defined" to "Not set",
        "change_phone.new_phone_section" to "New Number",
        "change_phone.new_phone" to "New Phone Number",
        "change_phone.phone_placeholder" to "+1 234 567 8901",
        "change_phone.leave_blank_to_remove" to "Leave blank to remove",
        "change_phone.confirm_with_password" to "Confirm with your password",
        "change_phone.save" to "Save",
        "change_phone.saving" to "Saving...",
        "change_phone.password_required" to "Password is required",
        "change_phone.error" to "Error changing phone",
        
        // ========================================
        // SECURITY SCREEN
        // ========================================
        "security.title" to "Security",
        "security.change_password" to "Change Password",
        "security.current_password" to "Current Password",
        "security.new_password" to "New Password",
        "security.confirm_password" to "Confirm Password",
        "security.password_requirements" to "At least 8 characters",
        "security.change_password_button" to "Change Password",
        "security.changing" to "Changing...",
        "security.all_fields_required" to "All fields are required",
        "security.passwords_do_not_match" to "Passwords do not match",
        "security.password_too_short" to "Password must be at least 8 characters",
        "security.change_password_error" to "Error changing password",
        "security.password_changed" to "Password changed successfully",
        "security.active_sessions" to "Active Sessions",
        "security.no_active_sessions" to "No active sessions",
        "security.current_session" to "This session",
        "security.other_session" to "Other device",
        "security.current_badge" to "Current",
        "security.created_at" to "Created",
        "security.expires_at" to "Expires",
        "security.revoke" to "Revoke",
        "security.revoke_all" to "Revoke All",
        "security.revoke_session_title" to "Revoke Session",
        "security.revoke_session_message" to "Are you sure you want to disconnect this device?",
        "security.revoke_all_sessions" to "Disconnect All Devices",
        "security.revoke_all_sessions_title" to "Disconnect All Devices",
        "security.revoke_all_sessions_message" to "Are you sure you want to disconnect all other devices? You will need to log in again on each of them.",
        "security.session_revoked" to "Session revoked successfully",
        "security.all_sessions_revoked" to "All other sessions have been revoked",
        "security.revoke_session_error" to "Error revoking session",
        "security.revoke_all_sessions_error" to "Error revoking sessions",
        "security.security_tip" to "Tip: Change your password regularly and disconnect devices you don't recognize.",
        
        // Password strength indicator
        "security.password_strength" to "Password strength",
        "security.password_weak" to "Weak",
        "security.password_fair" to "Fair",
        "security.password_good" to "Good",
        "security.password_strong" to "Strong",
        
        // Security status
        "security.security_status" to "Security Status",
        "security.account_protected" to "Your account is protected",
        "security.active_devices" to "Active devices",
        "security.password_set" to "Password set",
        
        // Real-time updates
        "security.real_time_updates" to "Real-time updates",
        
        // Enhanced security tips
        "security.security_tips_title" to "Security Tips",
        "security.tip_1" to "Use a unique password of at least 12 characters with uppercase, lowercase, numbers and symbols.",
        "security.tip_2" to "Never share your password and enable two-factor authentication if available.",
        "security.tip_3" to "Regularly check your active sessions and disconnect any suspicious devices.",
        
        // ========================================
        // DELETE ACCOUNT
        // ========================================
        "delete_account.title" to "Delete Account",
        "delete_account.warning" to "This action is irreversible. All your data will be permanently deleted.",
        "delete_account.confirm_title" to "Delete your account?",
        "delete_account.confirm_message" to "This action will permanently delete your account and all your data (bookings, reviews, photos, etc.). This action is irreversible.",
        "delete_account.password_label" to "Enter your password to confirm",
        "delete_account.confirm_checkbox" to "I understand this action is irreversible",
        "delete_account.delete_button" to "Delete my account",
        "delete_account.deleting" to "Deleting...",
        "delete_account.success" to "Your account has been successfully deleted",
        "delete_account.error" to "Error deleting account",
        
        // ========================================
        // PAYMENT METHODS SCREEN
        // ========================================
        "payment_methods.title" to "Payment Methods",
        "payment_methods.no_cards" to "No cards saved",
        "payment_methods.description" to "Your payment cards will be securely saved during your next purchase.",
        "payment_methods.stripe_info" to "Your payment information is secured by Stripe. We never store your card numbers.",
        "payment_methods.add_card" to "Add a card",
        
        // ========================================
        // BLOCKED USERS SCREEN
        // ========================================
        "blocked_users.title" to "Blocked Users",
        "blocked_users.no_blocked_users" to "No blocked users",
        "blocked_users.description" to "Users you block won't be able to see your profile or contact you.",
        "blocked_users.unblock" to "Unblock",
        
        // ========================================
        // HELP CENTER SCREEN
        // ========================================
        "help_center.title" to "Help Center",
        "help_center.faq_title" to "Frequently Asked Questions",
        "help_center.need_more_help" to "Didn't find an answer?",
        "help_center.contact_support" to "Contact Support",
        "help_center.faq1_question" to "How do I cancel a booking?",
        "help_center.faq1_answer" to "Go to 'My Bookings', select the booking and tap 'Cancel'. Cancellation is free up to 24 hours before the appointment.",
        "help_center.faq2_question" to "How do I edit my profile?",
        "help_center.faq2_answer" to "Go to Settings > My Account > Edit Profile to update your personal information.",
        "help_center.faq3_question" to "How do I contact a salon?",
        "help_center.faq3_answer" to "On the salon page, you'll find contact information: phone, email and address.",
        "help_center.faq4_question" to "Are payments secure?",
        "help_center.faq4_answer" to "Yes, all payments are processed by Stripe, a world leader in online payments. Your banking information is never stored on our servers.",
        
        // ========================================
        // CONTACT SUPPORT SCREEN
        // ========================================
        "contact.title" to "Contact Support",
        "contact.response_time" to "We typically respond within 24-48 hours.",
        "contact.send_message" to "Send a message",
        "contact.subject" to "Subject",
        "contact.message" to "Your message",
        "contact.send" to "Send",
        "contact.sending" to "Sending...",
        
        // ========================================
        // TERMS OF SERVICE
        // ========================================
        "terms.title" to "Terms of Service",
        "terms.last_update" to "Last updated: January 2025",
        "terms.section1_title" to "1. Acceptance of Terms",
        "terms.section1_content" to "By using the Frollot app, you agree to these terms of service. If you do not agree to these terms, please do not use the app.",
        "terms.section2_title" to "2. Service Description",
        "terms.section2_content" to "Frollot is a platform connecting hair salons with their clients. It enables online booking, payment and appointment tracking.",
        "terms.section3_title" to "3. User Account",
        "terms.section3_content" to "You are responsible for maintaining the confidentiality of your login credentials and all activities that occur under your account.",
        "terms.section4_title" to "4. Payments and Cancellations",
        "terms.section4_content" to "Payments are securely processed via Stripe. Cancellation policies vary by salon.",
        
        // ========================================
        // PRIVACY POLICY
        // ========================================
        "privacy.title" to "Privacy Policy",
        "privacy.last_update" to "Last updated: January 2025",
        "privacy.section1_title" to "1. Data Collected",
        "privacy.section1_content" to "We collect data you provide us (name, email, phone) as well as app usage data to improve our services.",
        "privacy.section2_title" to "2. Use of Data",
        "privacy.section2_content" to "Your data is used to manage your bookings, send you important notifications and improve your user experience.",
        "privacy.section3_title" to "3. Data Sharing",
        "privacy.section3_content" to "We share your booking information with the relevant salons. We never sell your personal data to third parties.",
        "privacy.section4_title" to "4. Your Rights",
        "privacy.section4_content" to "You have the right to access, modify or delete your personal data at any time through your account settings.",
        
        // ========================================
        // PAYMENT SCREEN
        // ========================================
        "payment.title" to "Payment",
        "payment.cancel" to "Cancel",
        "payment.service" to "Service:",
        "payment.salon" to "Salon:",
        "payment.date" to "Date:",
        
        // ========================================
        // REQUEST VERIFICATION SCREEN
        // ========================================
        "request_verification.title" to "Request verification",
        "request_verification.header_title" to "Verification request",
        "request_verification.description" to "Select the type of verification you want to obtain. Our team will review your request shortly.",
        "request_verification.additional_info" to "Additional information (optional)",
        "request_verification.additional_info_placeholder" to "Describe your situation, provide documents (SIRET, diplomas, etc.)...",
        "request_verification.verification_type" to "Verification type *",
        
        // ========================================
        // CREATE REVIEW SCREEN
        // ========================================
        "create_review.title" to "Leave a review",
        "create_review.your_booking" to "Your booking",
        "create_review.salon" to "Salon: {salonName}",
        "create_review.date" to "Date: {date}",
        "create_review.rating_question" to "How would you rate your experience?",
        "create_review.title_label" to "Review title (optional)",
        "create_review.title_placeholder" to "E.g.: Great experience!",
        "create_review.comment_label" to "Your comment (optional)",
        "create_review.comment_placeholder" to "Share your experience...",
        
        // ========================================
        // OWNER BOOKINGS MANAGEMENT SCREEN
        // ========================================
        "owner_bookings_management.title" to "Appointments Management",
        "owner_bookings_management.confirm" to "Confirm",
        "owner_bookings_management.start" to "Start",
        "owner_bookings_management.absent" to "Absent",
        "owner_bookings_management.finish" to "Finish",
        
        // ========================================
        // SEARCH SCREEN
        // ========================================
        "search.title" to "Search",
        "search.advanced_filters" to "Advanced filters",
        "search.post_type" to "Post type",
        "search.search_placeholder" to "Search for posts, salons, users...",
        "search.all" to "All",
        "search.load_more" to "Load more",
        "search.no_posts_found" to "No posts found",
        "search.no_salons_found" to "No salons found",
        "search.no_users_found" to "No users found",
        "search.no_hashtags_found" to "No hashtags found",
        "search.no_results_found" to "No results found",
        "search.posts" to "Posts ({count})",
        "search.salons" to "Salons ({count})",
        "search.users" to "Users ({count})",
        "search.hashtags" to "Hashtags ({count})",
        
        // ========================================
        // ULTRA PREMIUM POST CARD COMPONENT
        // ========================================
        "ultra_premium_post_card.oops" to "Oops!",
        "ultra_premium_post_card.retry" to "Retry",
        "ultra_premium_post_card.no_posts_yet" to "No posts yet",
        "ultra_premium_post_card.be_first_to_share" to "Be the first to share\nsomething amazing!",
        "ultra_premium_post_card.view_comments" to "View comment",
        "ultra_premium_post_card.view_comments_plural" to "View {count} comments",
        "ultra_premium_post_card.add_comment" to "Add a comment...",
        "ultra_premium_post_card.pinned" to "Pinned",
        
        // ========================================
        // COMMON BUTTONS AND ACTIONS
        // ========================================
        "common.cancel" to "Cancel",
        "common.save" to "Save",
        "common.confirm" to "Confirm",
        "common.retry" to "Retry",
        "common.delete" to "Delete",
        "common.edit" to "Edit",
        "common.remove" to "Remove",
        "common.add" to "Add",
        "common.create" to "Create",
        "common.load_more" to "Load more",
        "common.all" to "All",
        "common.start" to "Start",
        "common.finish" to "Finish",
        "common.absent" to "Absent",
        "common.popular" to "Popular",
        "common.public" to "Public",
        "common.service" to "Service:",
        "common.salon" to "Salon:",
        "common.date" to "Date:",
        "common.verified" to "Verified",
        
        // ========================================
        // UI COMPONENTS
        // ========================================
        // RatingBar
        "components.rating_bar.star_filled" to "Star {number} filled",
        "components.rating_bar.star_empty" to "Star {number} empty",
        "components.rating_bar.reviews_count" to "({count} reviews)",
        
        // PasswordTextField
        "components.password_text_field.label" to "Password",
        "components.password_text_field.placeholder" to "Enter your password",
        "components.password_text_field.show_password" to "Show password",
        "components.password_text_field.hide_password" to "Hide password",
        
        // ReportDialog
        "components.report_dialog.title" to "Report this {entity}",
        "components.report_dialog.info_message" to "Your report will be reviewed by our moderation team. Thank you for helping us maintain a respectful community.",
        "components.report_dialog.reason_label" to "Reason for reporting *",
        "components.report_dialog.additional_info_label" to "Additional information (optional)",
        "components.report_dialog.additional_info_placeholder" to "Briefly describe the problem...",
        "components.report_dialog.error_select_reason" to "Please select a reason",
        "components.report_dialog.error_reporting" to "Error reporting: {error}",
        "components.report_dialog.error_unknown" to "Unknown error",
        
        // UserAvatar
        "components.user_avatar.content_description" to "Avatar of {name}",
        
        // QueueStatusCard
        "components.queue_status_card.connection_lost" to "Connection lost",
        "components.queue_status_card.data_stale" to "Stale data",
        "components.queue_status_card.last_update" to "Last update: {minutes} min ago",
        "components.queue_status_card.your_progress" to "Your progress",
        "components.queue_status_card.current_position" to "Current position",
        "components.queue_status_card.estimated_time" to "Estimated time",
        "components.queue_status_card.just_now" to "Just now",
        "components.queue_status_card.minutes_ago" to "{minutes} min ago",
        "components.queue_status_card.leave_queue" to "Leave queue",
        "components.queue_status_card.reconnecting" to "Automatic reconnection in progress...",
        "components.queue_status_card.keep_app_open" to "Keep the app open to be notified by the salon.",
        "components.queue_status_card.status_offline" to "❌ Offline",
        "components.queue_status_card.status_pending" to "⏸️ Update pending...",
        "components.queue_status_card.status_auto_refresh" to "✅ Auto refresh (30s)",
        
        // FullScreenImageViewer
        "components.full_screen_image_viewer.image_content_description" to "Image {number}",
        "components.full_screen_image_viewer.close" to "Close",
        
        // AppDrawer
        "components.app_drawer.marketplace" to "Marketplace",
        "components.app_drawer.social" to "Social",
        "components.app_drawer.appointments" to "Appointments",
        "components.app_drawer.account" to "Account",
        "components.app_drawer.profile" to "Profile",
        "components.app_drawer.notifications" to "Notifications",
        "components.app_drawer.favorites" to "Favorites",
        "components.app_drawer.archives" to "Archives",
        "components.app_drawer.collections" to "Collections",
        "components.app_drawer.management" to "Management",
        "components.app_drawer.my_salons" to "My Salons",
        "components.app_drawer.new_salon" to "New Salon",
        "components.app_drawer.create_post" to "Create a post",
        "components.app_drawer.bookings_management" to "Appointments Management",
        "components.app_drawer.stats" to "Stats",
        "components.app_drawer.activity" to "Activity",
        "components.app_drawer.my_portfolios" to "My Portfolios",
        "components.app_drawer.new_portfolio" to "New Portfolio",
        "components.app_drawer.services" to "Services",
        "components.app_drawer.agenda" to "Agenda",
        "components.app_drawer.admin" to "Admin",
        "components.app_drawer.dashboard" to "Dashboard",
        "components.app_drawer.users" to "Users",
        "components.app_drawer.settings" to "Settings",
        "components.app_drawer.help" to "Help",
        "components.app_drawer.guest" to "Guest",
        "components.app_drawer.client" to "Client",
        
        // ========================================
        // PAYMENT SCREEN
        // ========================================
        "payment.title" to "Payment",
        "payment.cancel" to "Cancel",
        "payment.service" to "Service",
        "payment.salon" to "Salon",
        "payment.date" to "Date",
        "payment.card_input" to "Card",
        "payment.confirmation" to "Verification",
        "payment.processing" to "Processing...",
        "payment.success" to "Payment successful",
        "payment.error" to "Error",
        "payment.continue" to "Continue",
        "payment.retry" to "Retry",
        "payment.pay_amount" to "Pay {amount}€",
        "payment.verify_order" to "Verify your order",
        "payment.booking_details" to "Booking details",
        "payment.card" to "Card",
        "payment.total_to_pay" to "Total to pay",
        "payment.modify_card" to "Modify card",
        "payment.processing_message" to "Please wait while we process your payment securely.",
        "payment.success_message" to "Your booking is confirmed. You will receive a confirmation email.",
        "payment.error_message" to "An error occurred during payment. Please try again.",
        "payment.view_booking" to "View my booking",
        "payment.card_number" to "Card number",
        "payment.card_holder" to "Name on card",
        "payment.expiry" to "Expiry",
        "payment.cvv" to "CVV",
        "payment.ssl" to "SSL 256-bit",
        "payment.pci_dss" to "PCI-DSS",
        "payment.3d_secure" to "3D Secure",
        "payment.history" to "Payment history",
        "payment.no_payments" to "You haven't made any payments yet.",
        "payment.summary" to "Summary",
        "payment.total_spent" to "Total spent",
        "payment.transactions" to "Transactions",
        "payment.success_rate" to "Success rate",
        "payment.all_payments" to "All",
        "payment.succeeded" to "Succeeded",
        "payment.failed" to "Failed",
        "payment.refunded" to "Refunded",
        "payment.transaction_id" to "Transaction ID",
        "payment.stripe_reference" to "Stripe reference",
        "payment.payment_method" to "Method",
        "payment.currency" to "Currency",
        "payment.refunded_amount" to "Refunded",
        
        // Stripe Checkout
        "payment.redirect" to "Redirect",
        "payment.secure_payment" to "Secure Payment",
        "payment.secure_payment_description" to "You will be redirected to Stripe's secure payment page.",
        "payment.order_summary" to "Order Summary",
        "payment.total" to "Total",
        "payment.loading" to "Loading...",
        "payment.proceed_to_payment" to "Proceed to Payment",
        "payment.secure_ssl" to "SSL 256-bit",
        "payment.stripe_secure" to "Stripe",
        "payment.pci_compliant" to "PCI-DSS",
        "payment.redirect_to_stripe" to "Payment Page",
        "payment.redirect_description" to "Complete your payment on the Stripe page.\nReturn here after payment.",
        "payment.open_payment_page" to "Open Payment Page",
        "payment.check_payment_status" to "Check Payment Status",
        "payment.processing_payment" to "Processing...",
        "payment.processing_description" to "Please wait while we verify your payment.",
        "payment.payment_successful" to "Payment Successful! 🎉",
        "payment.payment_success_description" to "Your booking is confirmed.\nYou will receive a confirmation email.",
        "payment.payment_failed" to "Payment Failed",
        "payment.payment_failed_description" to "An error occurred during payment.\nPlease try again.",
        "payment.step_summary" to "Summary",
        "payment.step_payment" to "Payment",
        "payment.step_confirmation" to "Confirmation",
        
        "components.app_drawer.owner" to "Owner",
        "components.app_drawer.hairstylist" to "Hairstylist",
        "components.app_drawer.admin_user" to "Admin",
        
        // ExternalShareDialog
        "components.external_share_dialog.title" to "Share to",
        "components.external_share_dialog.share_via_app" to "Share via an app",
        "components.external_share_dialog.share_via_app_description" to "Instagram, WhatsApp, Messages, etc.",
        "components.external_share_dialog.error_sharing" to "Error sharing: {error}",
        "components.external_share_dialog.copy_link" to "Copy link",
        "components.external_share_dialog.copy_link_description" to "Copy the post link to clipboard",
        "components.external_share_dialog.error_copying" to "Error copying: {error}",
        "components.external_share_dialog.not_available" to "External sharing is not available on this platform",
        
        // SearchTextField
        "components.search_text_field.placeholder" to "Search...",
        "components.search_text_field.content_description" to "Search",
        
        // ========================================
        // ENUMS - LOCALIZATION
        // ========================================
        // BookingStatus
        "enums.booking_status.pending" to "Pending",
        "enums.booking_status.confirmed" to "Confirmed",
        "enums.booking_status.in_progress" to "In progress",
        "enums.booking_status.completed" to "Completed",
        "enums.booking_status.cancelled" to "Cancelled",
        "enums.booking_status.no_show" to "No show",
        
        // PaymentStatus
        "enums.payment_status.pending" to "Pending",
        "enums.payment_status.processing" to "Processing",
        "enums.payment_status.succeeded" to "Succeeded",
        "enums.payment_status.failed" to "Failed",
        "enums.payment_status.canceled" to "Canceled",
        "enums.payment_status.partially_refunded" to "Partially refunded",
        "enums.payment_status.unpaid" to "Unpaid",
        "enums.payment_status.paid" to "Paid",
        "enums.payment_status.refunded" to "Refunded",
        
        // PostType
        "enums.post_type.general" to "General",
        "enums.post_type.avant_apres" to "Before/After",
        "enums.post_type.portfolio" to "Portfolio",
        "enums.post_type.tendance" to "Trend",
        "enums.post_type.conseil" to "Advice",
        "enums.post_type.realisation" to "Achievement",
        "enums.post_type.inspiration" to "Inspiration",
        "enums.post_type.general_description" to "General post",
        "enums.post_type.avant_apres_description" to "Show a before/after transformation",
        "enums.post_type.portfolio_description" to "Add to your portfolio",
        "enums.post_type.tendance_description" to "Share a hair trend",
        "enums.post_type.conseil_description" to "Give tips and advice",
        "enums.post_type.realisation_description" to "Show an achievement",
        "enums.post_type.inspiration_description" to "Share an inspiration",
        
        // PostVisibility
        "enums.post_visibility.public" to "Public",
        "enums.post_visibility.followers" to "Followers only",
        "enums.post_visibility.private" to "Private",
        "enums.post_visibility.public_description" to "Visible to everyone",
        "enums.post_visibility.followers_description" to "Visible only to your followers",
        "enums.post_visibility.private_description" to "Visible only to you",
        
        // ServiceCategory
        "enums.service_category.coupe" to "Cut & Style",
        "enums.service_category.coloration" to "Coloring",
        "enums.service_category.soin" to "Care",
        "enums.service_category.coiffage" to "Styling",
        "enums.service_category.barbe" to "Barber",
        "enums.service_category.technique" to "Special Techniques",
        "enums.service_category.autre" to "Other Services",
        
        // ReactionType
        "enums.reaction_type.like" to "Like",
        "enums.reaction_type.love" to "Love",
        "enums.reaction_type.wow" to "Wow",
        "enums.reaction_type.inspirant" to "Inspiring",
        "enums.reaction_type.magnifique" to "Magnificent",
        "enums.reaction_type.bravo" to "Bravo",
        "enums.reaction_type.like_description" to "Classic like",
        "enums.reaction_type.love_description" to "I love this color!",
        "enums.reaction_type.wow_description" to "Incredible transformation!",
        "enums.reaction_type.inspirant_description" to "I want the same thing!",
        "enums.reaction_type.magnifique_description" to "Quality work!",
        "enums.reaction_type.bravo_description" to "Congratulations to the hairstylist!",
        
        // MediaType
        "enums.media_type.before" to "Before",
        "enums.media_type.after" to "After",
        "enums.media_type.process" to "Process",
        "enums.media_type.detail" to "Detail",
        
        // ReportReason
        "enums.report_reason.inapproprie" to "Inappropriate content",
        "enums.report_reason.spam" to "Advertising spam",
        "enums.report_reason.faux" to "Fake before/after",
        "enums.report_reason.copyright" to "Copyright violation",
        "enums.report_reason.autre" to "Other",
        "enums.report_reason.inapproprie_description" to "Violent, harassing, or offensive content",
        "enums.report_reason.spam_description" to "Unsolicited advertising or repetitive content",
        "enums.report_reason.faux_description" to "Misleading transformation or result",
        "enums.report_reason.copyright_description" to "Unauthorized use of protected content",
        "enums.report_reason.autre_description" to "Other reason to specify",
        
        // ReportedEntityType
        "enums.reported_entity_type.post" to "post",
        "enums.reported_entity_type.comment" to "comment",
        "enums.reported_entity_type.user" to "user",
        "enums.reported_entity_type.salon" to "salon",
        
        // VerificationType
        "enums.verification_type.email" to "Verified email",
        "enums.verification_type.phone" to "Verified phone",
        "enums.verification_type.business" to "Verified business",
        "enums.verification_type.professional" to "Verified professional",
        "enums.verification_type.email_description" to "Email verified by confirmation",
        "enums.verification_type.phone_description" to "Phone number verified",
        "enums.verification_type.business_description" to "Business verified (SIRET, documents)",
        "enums.verification_type.professional_description" to "Diplomas and certifications verified",
        
        // BadgeCategory
        "enums.badge_category.certification" to "Certification",
        "enums.badge_category.competition" to "Competition",
        "enums.badge_category.formation" to "Training",
        "enums.badge_category.partenariat" to "Partnership",
        
        // ReportStatus
        "enums.report_status.pending" to "Pending",
        "enums.report_status.reviewed" to "Under review",
        "enums.report_status.resolved" to "Resolved",
        "enums.report_status.dismissed" to "Dismissed",
        
        // ModerationAction
        "enums.moderation_action.hide" to "Hide",
        "enums.moderation_action.delete" to "Delete",
        "enums.moderation_action.warn" to "Warn",
        "enums.moderation_action.hide_description" to "Content will be hidden from all users except the author and administrators.",
        "enums.moderation_action.delete_description" to "Content will be permanently deleted and cannot be restored.",
        "enums.moderation_action.warn_description" to "A warning will be sent to the author without modifying the content.",

        // AppealStatus
        "enums.appeal_status.none" to "No appeal",
        "enums.appeal_status.pending" to "Pending",
        "enums.appeal_status.approved" to "Approved",
        "enums.appeal_status.rejected" to "Rejected",

        // ModerationAppealStatus
        "enums.appeal_status.none" to "No appeal",
        "enums.appeal_status.pending" to "Pending",
        "enums.appeal_status.approved" to "Approved",
        "enums.appeal_status.rejected" to "Rejected",
        
        // HairHashtagCategory
        "enums.hair_hashtag_category.technique" to "Technique",
        "enums.hair_hashtag_category.style" to "Style",
        "enums.hair_hashtag_category.couleur" to "Color",
        "enums.hair_hashtag_category.longueur" to "Length",
        "enums.hair_hashtag_category.texture" to "Texture",
        
        // FollowingType
        "enums.following_type.coiffeur" to "Hairstylist",
        "enums.following_type.salon" to "Salon",
    )
    
    return StringsBundle(strings)
}
