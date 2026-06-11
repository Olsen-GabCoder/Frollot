package com.frollot.mobile.localization

/**
 * Structure hiérarchique type-safe pour toutes les clés de strings de l'application.
 * 
 * Cette structure permet :
 * - Autocomplétion IDE
 * - Vérification à la compilation (pas de typos)
 * - Refactoring sûr
 * - Organisation logique par écrans/composants
 * 
 * Conforme à l'ADR-001 - DÉCISION 8 : Structure hiérarchique type-safe.
 * 
 * NOTE : Cette structure est créée pour la Phase 1. Les clés seront progressivement
 * ajoutées lors de la migration des écrans (Phase 2).
 * 
 * Structure actuelle (vide pour l'instant, sera remplie lors des migrations) :
 * - Écrans (Login, Register, Home, etc.)
 * - Composants (Buttons, Dialogs, Forms, etc.)
 * - Enums (BookingStatus, PaymentStatus, etc.)
 * - Erreurs (Errors globaux)
 */
object Strings {
    // ========================================
    // ÉCRAN DE CONNEXION
    // ========================================
    object Login {
        val WelcomeTitle = StringKey("login.welcome_title")
        val WelcomeSubtitle = StringKey("login.welcome_subtitle")
        val EmailLabel = StringKey("login.email_label")
        val EmailPlaceholder = StringKey("login.email_placeholder")
        val PasswordLabel = StringKey("login.password_label")
        val PasswordPlaceholder = StringKey("login.password_placeholder")
        val ForgotPassword = StringKey("login.forgot_password")
        val SubmitButton = StringKey("login.submit_button")
        val SubmitButtonLoading = StringKey("login.submit_button_loading")
        val ContinueWith = StringKey("login.continue_with")
        val GoogleButton = StringKey("login.google_button")
        val FacebookButton = StringKey("login.facebook_button")
        val NewHere = StringKey("login.new_here")
        val CreateAccount = StringKey("login.create_account")
        val SecureConnection = StringKey("login.secure_connection")
        
        object Errors {
            val InvalidCredentials = StringKey("login.errors.invalid_credentials")
            val AccountDisabled = StringKey("login.errors.account_disabled")
            val AccountNotFound = StringKey("login.errors.account_not_found")
            val ServerUnavailable = StringKey("login.errors.server_unavailable")
            val Timeout = StringKey("login.errors.timeout")
            val GenericError = StringKey("login.errors.generic_error")
        }
    }
    
    // ========================================
    // ÉCRAN D'INSCRIPTION
    // ========================================
    // ========================================
    // ÉCRAN DE VÉRIFICATION EMAIL
    // ========================================
    object EmailVerification {
        val Title = StringKey("email_verification.title")
        val Subtitle = StringKey("email_verification.subtitle")
        val Instructions = StringKey("email_verification.instructions")
        val TokenLabel = StringKey("email_verification.token_label")
        val TokenPlaceholder = StringKey("email_verification.token_placeholder")
        val VerifyButton = StringKey("email_verification.verify_button")
        val VerifyButtonLoading = StringKey("email_verification.verify_button_loading")
        val ResendButton = StringKey("email_verification.resend_button")
        val ResendButtonLoading = StringKey("email_verification.resend_button_loading")
        val SuccessTitle = StringKey("email_verification.success_title")
        val SuccessMessage = StringKey("email_verification.success_message")
        val ResendSuccess = StringKey("email_verification.resend_success")
        val ResendInstructions = StringKey("email_verification.resend_instructions")
        
        object Errors {
            val InvalidToken = StringKey("email_verification.errors.invalid_token")
            val ExpiredToken = StringKey("email_verification.errors.expired_token")
            val ServerError = StringKey("email_verification.errors.server_error")
            val GenericError = StringKey("email_verification.errors.generic_error")
        }
    }
    
    // ========================================
    // ÉCRAN MOT DE PASSE OUBLIÉ
    // ========================================
    object ForgotPassword {
        val Title = StringKey("forgot_password.title")
        val Instructions = StringKey("forgot_password.instructions")
        val EmailLabel = StringKey("forgot_password.email_label")
        val SendResetLinkButton = StringKey("forgot_password.send_reset_link_button")
        val SendingEmailButton = StringKey("forgot_password.sending_email_button")
        val EmailRequiredError = StringKey("forgot_password.email_required_error")
        val EmailSentConfirmation = StringKey("forgot_password.email_sent_confirmation")
        val BackButtonDescription = StringKey("common.back_button_description") // Clé commune
    }
    
    // ========================================
    // ÉCRAN RÉINITIALISER MOT DE PASSE
    // ========================================
    object ResetPassword {
        val Title = StringKey("reset_password.title")
        val Instructions = StringKey("reset_password.instructions")
        val NewPasswordLabel = StringKey("reset_password.new_password_label")
        val ConfirmPasswordLabel = StringKey("reset_password.confirm_password_label")
        val ResetPasswordButton = StringKey("reset_password.reset_password_button")
        val ResettingPasswordButton = StringKey("reset_password.resetting_password_button")
        val PasswordsDoNotMatchError = StringKey("reset_password.passwords_do_not_match_error")
        val PasswordFieldsRequiredError = StringKey("reset_password.password_fields_required_error")
        val PasswordResetSuccessMessage = StringKey("reset_password.password_reset_success_message")
        val TokenMissingError = StringKey("reset_password.token_missing_error")
        val BackButtonDescription = StringKey("common.back_button_description") // Clé commune
    }
    
    object Register {
        val WelcomeTitle = StringKey("register.welcome_title")
        val WelcomeSubtitle = StringKey("register.welcome_subtitle")
        val FirstNameLabel = StringKey("register.first_name_label")
        val FirstNamePlaceholder = StringKey("register.first_name_placeholder")
        val LastNameLabel = StringKey("register.last_name_label")
        val LastNamePlaceholder = StringKey("register.last_name_placeholder")
        val EmailLabel = StringKey("register.email_label")
        val EmailPlaceholder = StringKey("register.email_placeholder")
        val AccountTypeLabel = StringKey("register.account_type_label")
        val PasswordLabel = StringKey("register.password_label")
        val PasswordPlaceholder = StringKey("register.password_placeholder")
        val ConfirmPasswordLabel = StringKey("register.confirm_password_label")
        val ConfirmPasswordPlaceholder = StringKey("register.confirm_password_placeholder")
        val PasswordMismatch = StringKey("register.password_mismatch")
        val SubmitButton = StringKey("register.submit_button")
        val SubmitButtonLoading = StringKey("register.submit_button_loading")
        val SignUpWith = StringKey("register.sign_up_with")
        val GoogleButton = StringKey("register.google_button")
        val FacebookButton = StringKey("register.facebook_button")
        val AlreadyRegistered = StringKey("register.already_registered")
        val LoginLink = StringKey("register.login_link")
        val DataProtected = StringKey("register.data_protected")
        val SuccessMessage = StringKey("register.success_message")
        
        object UserTypes {
            val Client = StringKey("register.user_types.client")
            val Hairstylist = StringKey("register.user_types.hairstylist")
            val SalonOwner = StringKey("register.user_types.salon_owner")
            val Admin = StringKey("register.user_types.admin")
        }
        
        object Errors {
            val EmailAlreadyUsed = StringKey("register.errors.email_already_used")
            val InvalidData = StringKey("register.errors.invalid_data")
            val ServerUnavailable = StringKey("register.errors.server_unavailable")
            val Timeout = StringKey("register.errors.timeout")
            val GenericError = StringKey("register.errors.generic_error")
        }
    }
    
    // ========================================
    // ÉCRAN D'ACCUEIL
    // ========================================
    object Home {
        val Title = StringKey("home.title")
        val FiltersButton = StringKey("home.filters_button")
        val MyBookingsButton = StringKey("home.my_bookings_button")
        val CreateSalonButton = StringKey("home.create_salon_button")
        val LoadingMessage = StringKey("home.loading_message")
        val ErrorMessage = StringKey("home.error_message")
        val EmptyStateTitle = StringKey("home.empty_state_title")
        val EmptyStateTitleWithFilters = StringKey("home.empty_state_title_with_filters")
        val EmptyStateMessage = StringKey("home.empty_state_message")
        val EmptyStateMessageWithFilters = StringKey("home.empty_state_message_with_filters")
        val CreateMySalon = StringKey("home.create_my_salon")
        val PremiumSalons = StringKey("home.premium_salons")
        val NearMe = StringKey("home.near_me")
        val City = StringKey("home.city")
        val SalonsFound = StringKey("home.salons_found")
        val SalonsFoundPlural = StringKey("home.salons_found_plural")
        val FilterByCity = StringKey("home.filter_by_city")
        val FilterByCityDescription = StringKey("home.filter_by_city_description")
        val CityPlaceholder = StringKey("home.city_placeholder")
        val Apply = StringKey("home.apply")
        val Reset = StringKey("home.reset")
        val PremiumBadge = StringKey("home.premium_badge")
        val Rating = StringKey("home.rating")
        val OpenHours = StringKey("home.open_hours")
        val Cut = StringKey("home.cut")
        val Coloring = StringKey("home.coloring")
        val Book = StringKey("home.book")
        val CoverPhotoDescription = StringKey("home.cover_photo_description")
    }
    
    // ========================================
    // ÉCRAN DE PARAMÈTRES
    // ========================================
    object Settings {
        val Title = StringKey("settings.title")
        
        // Sections
        object Sections {
            val Account = StringKey("settings.sections.account")
            val Privacy = StringKey("settings.sections.privacy")
            val Notifications = StringKey("settings.sections.notifications")
            val Appearance = StringKey("settings.sections.appearance")
            val SocialNetwork = StringKey("settings.sections.social_network")
            val Bookings = StringKey("settings.sections.bookings")
            val ContentAndMedia = StringKey("settings.sections.content_and_media")
            val DataPrivacy = StringKey("settings.sections.data_privacy")
            val HelpAndSupport = StringKey("settings.sections.help_and_support")
            val About = StringKey("settings.sections.about")
        }
        
        // Compte
        object Account {
            val Profile = StringKey("settings.account.profile")
            val ProfileSubtitle = StringKey("settings.account.profile_subtitle")
            val Security = StringKey("settings.account.security")
            val SecuritySubtitle = StringKey("settings.account.security_subtitle")
            val Email = StringKey("settings.account.email")
            val Phone = StringKey("settings.account.phone")
            val PhoneSubtitle = StringKey("settings.account.phone_subtitle")
            val NotDefined = StringKey("settings.account.not_defined")
        }
        
        // Confidentialité
        object Privacy {
            val ProfileVisibility = StringKey("settings.privacy.profile_visibility")
            val WhoCanFollowMe = StringKey("settings.privacy.who_can_follow_me")
            val WhoCanMessageMe = StringKey("settings.privacy.who_can_message_me")
            val ShowActivityStatus = StringKey("settings.privacy.show_activity_status")
            val ShowActivityStatusSubtitle = StringKey("settings.privacy.show_activity_status_subtitle")
            val BlockedUsers = StringKey("settings.privacy.blocked_users")
            val BlockedUsersCount = StringKey("settings.privacy.blocked_users_count")
            val NoBlockedUsers = StringKey("settings.privacy.no_blocked_users")
            
            object Options {
                val Public = StringKey("settings.privacy.options.public")
                val FollowersOnly = StringKey("settings.privacy.options.followers_only")
                val Private = StringKey("settings.privacy.options.private")
                val Everyone = StringKey("settings.privacy.options.everyone")
                val Nobody = StringKey("settings.privacy.options.nobody")
            }
        }
        
        // Notifications
        object Notifications {
            val Title = StringKey("settings.notifications.title")
            val Subtitle = StringKey("settings.notifications.subtitle")
            val Push = StringKey("settings.notifications.push")
            val PushSubtitle = StringKey("settings.notifications.push_subtitle")
            val Email = StringKey("settings.notifications.email")
            val EmailSubtitle = StringKey("settings.notifications.email_subtitle")
            val Bookings = StringKey("settings.notifications.bookings")
            val BookingsSubtitle = StringKey("settings.notifications.bookings_subtitle")
            val Social = StringKey("settings.notifications.social")
            val SocialSubtitle = StringKey("settings.notifications.social_subtitle")
            val Marketing = StringKey("settings.notifications.marketing")
            val MarketingSubtitle = StringKey("settings.notifications.marketing_subtitle")
        }
        
        // Apparence
        object Appearance {
            val DarkMode = StringKey("settings.appearance.dark_mode")
            val DarkModeSubtitle = StringKey("settings.appearance.dark_mode_subtitle")
            val Language = StringKey("settings.appearance.language")
        }
        
        // Réseau social
        object SocialNetwork {
            val PostVisibilityDefault = StringKey("settings.social_network.post_visibility_default")
            val AllowComments = StringKey("settings.social_network.allow_comments")
            val AllowCommentsSubtitle = StringKey("settings.social_network.allow_comments_subtitle")
            val AllowReactions = StringKey("settings.social_network.allow_reactions")
            val AllowReactionsSubtitle = StringKey("settings.social_network.allow_reactions_subtitle")
            val AllowShares = StringKey("settings.social_network.allow_shares")
            val AllowSharesSubtitle = StringKey("settings.social_network.allow_shares_subtitle")
        }
        
        // Réservations
        object Bookings {
            val BookingNotifications = StringKey("settings.bookings.booking_notifications")
            val BookingNotificationsSubtitle = StringKey("settings.bookings.booking_notifications_subtitle")
            val AvailabilityPreferences = StringKey("settings.bookings.availability_preferences")
            val AvailabilityPreferencesSubtitle = StringKey("settings.bookings.availability_preferences_subtitle")
            val PaymentMethods = StringKey("settings.bookings.payment_methods")
            val PaymentMethodsSubtitle = StringKey("settings.bookings.payment_methods_subtitle")
        }
        
        // Contenu et médias
        object ContentAndMedia {
            val AutoSavePhotos = StringKey("settings.content_and_media.auto_save_photos")
            val AutoSavePhotosSubtitle = StringKey("settings.content_and_media.auto_save_photos_subtitle")
            val DataUsage = StringKey("settings.content_and_media.data_usage")
            val VideoQuality = StringKey("settings.content_and_media.video_quality")
            
            object DataUsageOptions {
                val Economical = StringKey("settings.content_and_media.data_usage_options.economical")
                val Standard = StringKey("settings.content_and_media.data_usage_options.standard")
                val High = StringKey("settings.content_and_media.data_usage_options.high")
            }
            
            object VideoQualityOptions {
                val SD = StringKey("settings.content_and_media.video_quality_options.sd")
                val HD = StringKey("settings.content_and_media.video_quality_options.hd")
                val FullHD = StringKey("settings.content_and_media.video_quality_options.full_hd")
            }
        }
        
        // Confidentialité des données
        object DataPrivacy {
            val DownloadData = StringKey("settings.data_privacy.download_data")
            val DownloadDataSubtitle = StringKey("settings.data_privacy.download_data_subtitle")
            val DeleteAccount = StringKey("settings.data_privacy.delete_account")
            val DeleteAccountSubtitle = StringKey("settings.data_privacy.delete_account_subtitle")
        }
        
        // Aide et support
        object HelpAndSupport {
            val HelpCenter = StringKey("settings.help_and_support.help_center")
            val HelpCenterSubtitle = StringKey("settings.help_and_support.help_center_subtitle")
            val ContactUs = StringKey("settings.help_and_support.contact_us")
            val ContactUsSubtitle = StringKey("settings.help_and_support.contact_us_subtitle")
            val ReportBug = StringKey("settings.help_and_support.report_bug")
            val ReportBugSubtitle = StringKey("settings.help_and_support.report_bug_subtitle")
            val RateApp = StringKey("settings.help_and_support.rate_app")
            val RateAppSubtitle = StringKey("settings.help_and_support.rate_app_subtitle")
        }
        
        // À propos
        object About {
            val Version = StringKey("settings.about.version")
            val TermsOfService = StringKey("settings.about.terms_of_service")
            val TermsOfServiceSubtitle = StringKey("settings.about.terms_of_service_subtitle")
            val PrivacyPolicy = StringKey("settings.about.privacy_policy")
            val PrivacyPolicySubtitle = StringKey("settings.about.privacy_policy_subtitle")
            val Licenses = StringKey("settings.about.licenses")
            val LicensesSubtitle = StringKey("settings.about.licenses_subtitle")
        }
        
        // Actions
        object Actions {
            val Logout = StringKey("settings.actions.logout")
            val LogoutDialogTitle = StringKey("settings.actions.logout_dialog_title")
            val LogoutDialogText = StringKey("settings.actions.logout_dialog_text")
            val Delete = StringKey("settings.actions.delete")
            val DeleteAccountDialogTitle = StringKey("settings.actions.delete_account_dialog_title")
            val DeleteAccountDialogText = StringKey("settings.actions.delete_account_dialog_text")
            val Cancel = StringKey("settings.actions.cancel")
        }
    }
    
    // ========================================
    // ÉCRAN DE PROFIL
    // ========================================
    object Profile {
        val Version = StringKey("profile.version")
        val LogoutDialogTitle = StringKey("profile.logout_dialog_title")
        val LogoutDialogText = StringKey("profile.logout_dialog_text")
        val LogoutConfirm = StringKey("profile.logout_confirm")
        val Cancel = StringKey("profile.cancel")
        val AvatarPreview = StringKey("profile.avatar_preview")
        val Save = StringKey("profile.save")
        val EditPhoto = StringKey("profile.edit_photo")
        val AccountVerified = StringKey("profile.account_verified")
        val AccountNotVerified = StringKey("profile.account_not_verified")
        val AccountInfo = StringKey("profile.account_info")
        val Email = StringKey("profile.email")
        val Phone = StringKey("profile.phone")
        val MemberSince = StringKey("profile.member_since")
        val AccountType = StringKey("profile.account_type")
        val EditProfile = StringKey("profile.edit_profile")
        val EditProfileSubtitle = StringKey("profile.edit_profile_subtitle")
        val ChangePassword = StringKey("profile.change_password")
        val ChangePasswordSubtitle = StringKey("profile.change_password_subtitle")
        val Logout = StringKey("profile.logout")
        val LogoutSubtitle = StringKey("profile.logout_subtitle")
        val Statistics = StringKey("profile.statistics")
        val Salons = StringKey("profile.salons")
        val Bookings = StringKey("profile.bookings")
        val Reviews = StringKey("profile.reviews")
        val Services = StringKey("profile.services")
        val Points = StringKey("profile.points")
        
        object UserTypes {
            val Client = StringKey("profile.user_types.client")
            val SalonOwner = StringKey("profile.user_types.salon_owner")
            val Hairstylist = StringKey("profile.user_types.hairstylist")
            val Admin = StringKey("profile.user_types.admin")
        }
    }
    
    // ========================================
    // ÉCRAN DE FIL D'ACTUALITÉ SOCIAL
    // ========================================
    object SocialFeed {
        val FilterByType = StringKey("social_feed.filter_by_type")
        val MyFollows = StringKey("social_feed.my_follows")
        val NearMe = StringKey("social_feed.near_me")
        val All = StringKey("social_feed.all")
        val AddComment = StringKey("social_feed.add_comment")
        val Share = StringKey("social_feed.share")
        val Cancel = StringKey("social_feed.cancel")
        val NoCollectionYet = StringKey("social_feed.no_collection_yet")
        val Save = StringKey("social_feed.save")
        val Archive = StringKey("social_feed.archive")
        val ShareInApp = StringKey("social_feed.share_in_app")
        val ShareExternal = StringKey("social_feed.share_external")
        val Report = StringKey("social_feed.report")
        val AddToCollectionExample = StringKey("social_feed.add_to_collection_example")
        val AddToCollection = StringKey("social_feed.add_to_collection")
        val CreateCollection = StringKey("social_feed.create_collection")
    }
    
    // ========================================
    // ÉCRAN MES RÉSERVATIONS
    // ========================================
    object MyBookings {
        val Service = StringKey("my_bookings.service")
        val DateTime = StringKey("my_bookings.date_time")
        val Hairstylist = StringKey("my_bookings.hairstylist")
        val Amount = StringKey("my_bookings.amount")
        val ReviewLeft = StringKey("my_bookings.review_left")
        val LeaveReview = StringKey("my_bookings.leave_review")
        val Cancel = StringKey("my_bookings.cancel")
        val CancelConfirm = StringKey("my_bookings.cancel_confirm")
        val CancelKeep = StringKey("my_bookings.cancel_keep")
    }
    
    // ========================================
    // ÉCRAN DE RÉSERVATION (WIZARD)
    // ========================================
    object Booking {
        val BackToHome = StringKey("booking.back_to_home")
        val Duration = StringKey("booking.duration")
        val Category = StringKey("booking.category")
        val ChooseExpert = StringKey("booking.choose_expert")
        val ChooseExpertSubtitle = StringKey("booking.choose_expert_subtitle")
        val SelectDate = StringKey("booking.select_date")
        val SelectDateSubtitle = StringKey("booking.select_date_subtitle")
        val ChooseTimeSlot = StringKey("booking.choose_time_slot")
        val ChooseTimeSlotSubtitle = StringKey("booking.choose_time_slot_subtitle")
        val NotesOrSpecialRequests = StringKey("booking.notes_or_special_requests")
        val NotesOrSpecialRequestsSubtitle = StringKey("booking.notes_or_special_requests_subtitle")
        val NotesInfo = StringKey("booking.notes_info")
        val ExpertSelected = StringKey("booking.expert_selected")
        val Continue = StringKey("booking.continue")
        val TimeSlotSelected = StringKey("booking.time_slot_selected")
        val PayNow = StringKey("booking.pay_now")
        val ViewMyBookings = StringKey("booking.view_my_bookings")
    }
    
    // ========================================
    // ÉCRAN DE DÉTAIL DE RÉSERVATION
    // ========================================
    object BookingDetail {
        val CancelKeep = StringKey("booking_detail.cancel_keep")
        val CancelBookingTitle = StringKey("booking_detail.cancel_booking_title")
        val CancelConfirmMessage = StringKey("booking_detail.cancel_confirm_message")
        val BookingStatus = StringKey("booking_detail.booking_status")
        val HairSalon = StringKey("booking_detail.hair_salon")
        val ClickForDetails = StringKey("booking_detail.click_for_details")
        val Service = StringKey("booking_detail.service")
        val ServiceDetails = StringKey("booking_detail.service_details")
        val Booking = StringKey("booking_detail.booking")
        val BookingDetails = StringKey("booking_detail.booking_details")
        val Payment = StringKey("booking_detail.payment")
        val PaymentDetails = StringKey("booking_detail.payment_details")
        val Amount = StringKey("booking_detail.amount")
    }
    
    // ========================================
    // ÉCRAN DE DÉTAIL DE SALON
    // ========================================
    object SalonDetail {
        val Services = StringKey("salon_detail.services")
        val ViewPosts = StringKey("salon_detail.view_posts")
        val OpenFeed = StringKey("salon_detail.open_feed")
        val AddMember = StringKey("salon_detail.add_member")
        val Queue = StringKey("salon_detail.queue")
        val Position = StringKey("salon_detail.position")
        val LeaveQueue = StringKey("salon_detail.leave_queue")
        val JoinQueue = StringKey("salon_detail.join_queue")
        val LoginToJoinQueue = StringKey("salon_detail.login_to_join_queue")
        val Cancel = StringKey("salon_detail.cancel")
        val Subscribers = StringKey("salon_detail.subscribers")
        val Book = StringKey("salon_detail.book")
        val SalonVerified = StringKey("salon_detail.salon_verified")
        val Clients = StringKey("salon_detail.clients")
        val Waiting = StringKey("salon_detail.waiting")
        val YourPosition = StringKey("salon_detail.your_position")
        val PositionInQueue = StringKey("salon_detail.position_in_queue")
        val MyTeam = StringKey("salon_detail.my_team")
        val Team = StringKey("salon_detail.team")
        val OurServices = StringKey("salon_detail.our_services")
        val ChooseFromServices = StringKey("salon_detail.choose_from_services")
        val ChooseFromServicesPlural = StringKey("salon_detail.choose_from_services_plural")
        val LoadingServices = StringKey("salon_detail.loading_services")
        val PleaseWait = StringKey("salon_detail.please_wait")
        val LoadingError = StringKey("salon_detail.loading_error")
        val NoServicesAvailable = StringKey("salon_detail.no_services_available")
        val NoServicesMessage = StringKey("salon_detail.no_services_message")
        val ReadyToBook = StringKey("salon_detail.ready_to_book")
        val ReviewsAndRatings = StringKey("salon_detail.reviews_and_ratings")
    }
    
    // ========================================
    // ÉCRAN DE CRÉATION DE SALON
    // ========================================
    object CreateSalon {
        val LaunchYourSalon = StringKey("create_salon.launch_your_salon")
        val JoinCommunity = StringKey("create_salon.join_community")
        val SalonInfo = StringKey("create_salon.salon_info")
        val CoverPhoto = StringKey("create_salon.cover_photo")
        val CoverPhotoHint = StringKey("create_salon.cover_photo_hint")
        val AddPhoto = StringKey("create_salon.add_photo")
        val ClickToBrowse = StringKey("create_salon.click_to_browse")
        val DataSecured = StringKey("create_salon.data_secured")
    }
    
    // ========================================
    // ÉCRAN DE DÉTAIL DE POST
    // ========================================
    object PostDetail {
        val Comments = StringKey("post_detail.comments") // Utilisé avec pluralizedString
    }
    
    // ========================================
    // ÉCRAN DE CRÉATION DE POST
    // ========================================
    object CreatePost {
        val Public = StringKey("create_post.public")
        val PostType = StringKey("create_post.post_type")
        val Visibility = StringKey("create_post.visibility")
        val AtLeastTwoImages = StringKey("create_post.at_least_two_images")
        val ClickToSelectPhoto = StringKey("create_post.click_to_select_photo")
        val PhotoFormatHint = StringKey("create_post.photo_format_hint")
        val PostWillBeVisible = StringKey("create_post.post_will_be_visible")
    }
    
    // ========================================
    // ÉCRAN DE PROFIL COIFFEUR
    // ========================================
    object CoiffeurProfile {
        val PinnedPosts = StringKey("coiffeur_profile.pinned_posts")
        val RecentPosts = StringKey("coiffeur_profile.recent_posts")
        val Unfollow = StringKey("coiffeur_profile.unfollow")
        val Follow = StringKey("coiffeur_profile.follow")
        val BadgesAndCertifications = StringKey("coiffeur_profile.badges_and_certifications")
        val FeaturedPortfolio = StringKey("coiffeur_profile.featured_portfolio")
        val Portfolios = StringKey("coiffeur_profile.portfolios")
    }
    
    // ========================================
    // ÉCRAN DE PROFIL SOCIAL SALON
    // ========================================
    object SalonSocialProfile {
        val RecentPosts = StringKey("salon_social_profile.recent_posts")
        val Unfollow = StringKey("salon_social_profile.unfollow")
        val Follow = StringKey("salon_social_profile.follow")
        val FeaturedPosts = StringKey("salon_social_profile.featured_posts")
        val Portfolios = StringKey("salon_social_profile.portfolios")
        val Verified = StringKey("salon_social_profile.verified")
        val Services = StringKey("salon_social_profile.services")
    }
    
    // ========================================
    // ÉCRAN DE PROFIL CLIENT
    // ========================================
    object ClientProfile {
        val Title = StringKey("client_profile.title")
        val About = StringKey("client_profile.about")
        val Posts = StringKey("client_profile.posts")
        val Likes = StringKey("client_profile.likes")
        val Followers = StringKey("client_profile.followers")
        val Following = StringKey("client_profile.following")
        val Collections = StringKey("client_profile.collections")
        val CollectionsCount = StringKey("client_profile.collections_count")
        val RecentPosts = StringKey("client_profile.recent_posts")
        val Badges = StringKey("client_profile.badges")
        val Follow = StringKey("client_profile.follow")
        val Unfollow = StringKey("client_profile.unfollow")
    }
    
    // ========================================
    // ÉCRAN DE PROFIL PROPRIÉTAIRE DE SALON
    // ========================================
    object SalonOwnerProfile {
        val Title = StringKey("salon_owner_profile.title")
        val About = StringKey("salon_owner_profile.about")
        val Posts = StringKey("salon_owner_profile.posts")
        val Likes = StringKey("salon_owner_profile.likes")
        val Followers = StringKey("salon_owner_profile.followers")
        val Salons = StringKey("salon_owner_profile.salons")
        val Collections = StringKey("salon_owner_profile.collections")
        val CollectionsCount = StringKey("salon_owner_profile.collections_count")
        val RecentPosts = StringKey("salon_owner_profile.recent_posts")
        val Badges = StringKey("salon_owner_profile.badges")
        val Follow = StringKey("salon_owner_profile.follow")
        val Unfollow = StringKey("salon_owner_profile.unfollow")
    }
    
    // ========================================
    // ÉCRAN DE COMMENTAIRES
    // ========================================
    object Comments {
        val Title = StringKey("comments.title")
        val AddComment = StringKey("comments.add_comment")
        val LoadMore = StringKey("comments.load_more")
    }
    
    // ========================================
    // ÉCRAN DE COLLECTIONS
    // ========================================
    object Collections {
        val Title = StringKey("collections.title")
        val Post = StringKey("collections.post")
        val Posts = StringKey("collections.posts")
        val Edit = StringKey("collections.edit")
        val Delete = StringKey("collections.delete")
        val NewCollection = StringKey("collections.new_collection")
        val Name = StringKey("collections.name")
        val Description = StringKey("collections.description")
        val Category = StringKey("collections.category")
        val PublicCollection = StringKey("collections.public_collection")
        val SaveToCollection = StringKey("collections.save_to_collection")
        val NoCollections = StringKey("collections.no_collections")
    }
    
    // ========================================
    // ÉCRAN DE DÉTAIL DE COLLECTION
    // ========================================
    object CollectionDetail {
        val Title = StringKey("collection_detail.title")
        val LoadMore = StringKey("collection_detail.load_more")
        val Edit = StringKey("collection_detail.edit")
        val Delete = StringKey("collection_detail.delete")
        val DeleteCollection = StringKey("collection_detail.delete_collection")
        val DeleteCollectionMessage = StringKey("collection_detail.delete_collection_message")
        val RemoveFromCollection = StringKey("collection_detail.remove_from_collection")
    }
    
    // ========================================
    // ÉCRAN DE CRÉATION DE PORTFOLIO
    // ========================================
    object CreatePortfolio {
        val Title = StringKey("create_portfolio.title")
        val NoSalonFound = StringKey("create_portfolio.no_salon_found")
        val Salon = StringKey("create_portfolio.salon")
        val CoverImage = StringKey("create_portfolio.cover_image")
        val AddCoverImage = StringKey("create_portfolio.add_cover_image")
        val PublicPortfolio = StringKey("create_portfolio.public_portfolio")
        val Name = StringKey("create_portfolio.name")
        val NamePlaceholder = StringKey("create_portfolio.name_placeholder")
        val Description = StringKey("create_portfolio.description")
    }
    
    // ========================================
    // ÉCRAN DE DÉTAIL DE PORTFOLIO
    // ========================================
    object PortfolioDetail {
        val LoadMore = StringKey("portfolio_detail.load_more")
    }
    
    // ========================================
    // ÉCRAN DE LISTE DE PORTFOLIOS
    // ========================================
    object PortfoliosList {
        val CreatePortfolio = StringKey("portfolios_list.create_portfolio")
    }
    
    // ========================================
    // ÉCRAN DE CRÉATION DE SERVICE
    // ========================================
    object CreateService {
        val PremiumService = StringKey("create_service.premium_service")
        val ServiceInfo = StringKey("create_service.service_info")
        val Category = StringKey("create_service.category")
        val PriceAdjustmentInfo = StringKey("create_service.price_adjustment_info")
        val Name = StringKey("create_service.name")
        val NamePlaceholder = StringKey("create_service.name_placeholder")
        val Description = StringKey("create_service.description")
        val Duration = StringKey("create_service.duration")
        val Price = StringKey("create_service.price")
    }
    
    // ========================================
    // ÉCRAN DE POSTS DE SALON
    // ========================================
    object SalonPosts {
        val Title = StringKey("salon_posts.title")
        val All = StringKey("salon_posts.all")
        val AllServices = StringKey("salon_posts.all_services")
        val Popular = StringKey("salon_posts.popular")
    }
    
    // ========================================
    // ÉCRAN D'ARCHIVES
    // ========================================
    object Archives {
        val Title = StringKey("archives.title")
    }
    
    // ========================================
    // ÉCRAN DE FAVORIS
    // ========================================
    object Favorites {
        val Title = StringKey("favorites.title")
        val OfflineMode = StringKey("favorites.offline_mode")
    }
    
    // ========================================
    // ÉCRAN DE TENDANCES
    // ========================================
    object Trending {
        val Title = StringKey("trending.title")
        val Posts = StringKey("trending.posts")
        val Hashtags = StringKey("trending.hashtags")
        val Salons = StringKey("trending.salons")
    }
    
    // ========================================
    // ÉCRAN DE SIGNALEMENT
    // ========================================
    object Report {
        val InfoMessage = StringKey("report.info_message")
        val ReasonTitle = StringKey("report.reason_title")
        val AdditionalInfo = StringKey("report.additional_info")
        val Cancel = StringKey("report.cancel")
        val Submit = StringKey("report.submit")
        val ReportedContent = StringKey("report.reported_content")
        val PostAuthor = StringKey("report.post_author")
    }
    
    // ========================================
    // ÉCRAN DE CRÉATION DE STAFF
    // ========================================
    object CreateStaff {
        val Title = StringKey("create_staff.title")
        val NewCollaborator = StringKey("create_staff.new_collaborator")
        val PersonalInfo = StringKey("create_staff.personal_info")
        val Specialties = StringKey("create_staff.specialties")
        val SpecialtiesHint = StringKey("create_staff.specialties_hint")
        val EmailInfo = StringKey("create_staff.email_info")
        val FirstName = StringKey("create_staff.first_name")
        val FirstNamePlaceholder = StringKey("create_staff.first_name_placeholder")
        val LastName = StringKey("create_staff.last_name")
        val LastNamePlaceholder = StringKey("create_staff.last_name_placeholder")
        val Email = StringKey("create_staff.email")
    }
    
    // ========================================
    // ÉCRAN DE GESTION DE FILE D'ATTENTE
    // ========================================
    object QueueManagement {
        val Title = StringKey("queue_management.title")
        val Ticket = StringKey("queue_management.ticket")
        val Arrived = StringKey("queue_management.arrived")
        val EstimatedWait = StringKey("queue_management.estimated_wait")
        val CallNext = StringKey("queue_management.call_next")
        val Remove = StringKey("queue_management.remove")
    }
    
    // ========================================
    // ÉCRAN CHANGEMENT EMAIL
    // ========================================
    object ChangeEmail {
        val Title = StringKey("change_email.title")
        val CurrentEmail = StringKey("change_email.current_email")
        val NewEmailSection = StringKey("change_email.new_email_section")
        val NewEmail = StringKey("change_email.new_email")
        val ConfirmWithPassword = StringKey("change_email.confirm_with_password")
        val Save = StringKey("change_email.save")
        val Saving = StringKey("change_email.saving")
        val AllFieldsRequired = StringKey("change_email.all_fields_required")
        val InvalidEmail = StringKey("change_email.invalid_email")
        val Error = StringKey("change_email.error")
        val Info = StringKey("change_email.info")
    }
    
    // ========================================
    // ÉCRAN CHANGEMENT TÉLÉPHONE
    // ========================================
    object ChangePhone {
        val Title = StringKey("change_phone.title")
        val CurrentPhone = StringKey("change_phone.current_phone")
        val NotDefined = StringKey("change_phone.not_defined")
        val NewPhoneSection = StringKey("change_phone.new_phone_section")
        val NewPhone = StringKey("change_phone.new_phone")
        val PhonePlaceholder = StringKey("change_phone.phone_placeholder")
        val LeaveBlankToRemove = StringKey("change_phone.leave_blank_to_remove")
        val ConfirmWithPassword = StringKey("change_phone.confirm_with_password")
        val Save = StringKey("change_phone.save")
        val Saving = StringKey("change_phone.saving")
        val PasswordRequired = StringKey("change_phone.password_required")
        val Error = StringKey("change_phone.error")
    }
    
    // ========================================
    // ÉCRAN DE SÉCURITÉ
    // ========================================
    object Security {
        val Title = StringKey("security.title")
        
        // Changement de mot de passe
        val ChangePassword = StringKey("security.change_password")
        val CurrentPassword = StringKey("security.current_password")
        val NewPassword = StringKey("security.new_password")
        val ConfirmPassword = StringKey("security.confirm_password")
        val PasswordRequirements = StringKey("security.password_requirements")
        val ChangePasswordButton = StringKey("security.change_password_button")
        val Changing = StringKey("security.changing")
        val AllFieldsRequired = StringKey("security.all_fields_required")
        val PasswordsDoNotMatch = StringKey("security.passwords_do_not_match")
        val PasswordTooShort = StringKey("security.password_too_short")
        val ChangePasswordError = StringKey("security.change_password_error")
        val PasswordChanged = StringKey("security.password_changed")
        
        // Indicateur de force du mot de passe
        val PasswordStrength = StringKey("security.password_strength")
        val PasswordWeak = StringKey("security.password_weak")
        val PasswordFair = StringKey("security.password_fair")
        val PasswordGood = StringKey("security.password_good")
        val PasswordStrong = StringKey("security.password_strong")
        
        // Statut de sécurité
        val SecurityStatus = StringKey("security.security_status")
        val AccountProtected = StringKey("security.account_protected")
        val ActiveDevices = StringKey("security.active_devices")
        val PasswordSet = StringKey("security.password_set")
        
        // Sessions actives
        val ActiveSessions = StringKey("security.active_sessions")
        val RealTimeUpdates = StringKey("security.real_time_updates")
        val NoActiveSessions = StringKey("security.no_active_sessions")
        val CurrentSession = StringKey("security.current_session")
        val OtherSession = StringKey("security.other_session")
        val CurrentBadge = StringKey("security.current_badge")
        val CreatedAt = StringKey("security.created_at")
        val ExpiresAt = StringKey("security.expires_at")
        val Revoke = StringKey("security.revoke")
        val RevokeAll = StringKey("security.revoke_all")
        val RevokeSessionTitle = StringKey("security.revoke_session_title")
        val RevokeSessionMessage = StringKey("security.revoke_session_message")
        val RevokeAllSessions = StringKey("security.revoke_all_sessions")
        val RevokeAllSessionsTitle = StringKey("security.revoke_all_sessions_title")
        val RevokeAllSessionsMessage = StringKey("security.revoke_all_sessions_message")
        val SessionRevoked = StringKey("security.session_revoked")
        val AllSessionsRevoked = StringKey("security.all_sessions_revoked")
        val RevokeSessionError = StringKey("security.revoke_session_error")
        val RevokeAllSessionsError = StringKey("security.revoke_all_sessions_error")
        
        // Conseils de sécurité
        val SecurityTip = StringKey("security.security_tip")
        val SecurityTipsTitle = StringKey("security.security_tips_title")
        val Tip1 = StringKey("security.tip_1")
        val Tip2 = StringKey("security.tip_2")
        val Tip3 = StringKey("security.tip_3")
    }
    
    // ========================================
    // SUPPRESSION DE COMPTE
    // ========================================
    object DeleteAccount {
        val Title = StringKey("delete_account.title")
        val Warning = StringKey("delete_account.warning")
        val ConfirmTitle = StringKey("delete_account.confirm_title")
        val ConfirmMessage = StringKey("delete_account.confirm_message")
        val PasswordLabel = StringKey("delete_account.password_label")
        val ConfirmCheckbox = StringKey("delete_account.confirm_checkbox")
        val DeleteButton = StringKey("delete_account.delete_button")
        val Deleting = StringKey("delete_account.deleting")
        val Success = StringKey("delete_account.success")
        val Error = StringKey("delete_account.error")
    }
    
    // ========================================
    // ÉCRAN MÉTHODES DE PAIEMENT
    // ========================================
    object PaymentMethods {
        val Title = StringKey("payment_methods.title")
        val NoCards = StringKey("payment_methods.no_cards")
        val Description = StringKey("payment_methods.description")
        val StripeInfo = StringKey("payment_methods.stripe_info")
        val AddCard = StringKey("payment_methods.add_card")
    }
    
    // ========================================
    // ÉCRAN UTILISATEURS BLOQUÉS
    // ========================================
    object BlockedUsers {
        val Title = StringKey("blocked_users.title")
        val NoBlockedUsers = StringKey("blocked_users.no_blocked_users")
        val Description = StringKey("blocked_users.description")
        val Unblock = StringKey("blocked_users.unblock")
    }
    
    // ========================================
    // ÉCRAN CENTRE D'AIDE
    // ========================================
    object HelpCenter {
        val Title = StringKey("help_center.title")
        val FaqTitle = StringKey("help_center.faq_title")
        val NeedMoreHelp = StringKey("help_center.need_more_help")
        val ContactSupport = StringKey("help_center.contact_support")
        val Faq1Question = StringKey("help_center.faq1_question")
        val Faq1Answer = StringKey("help_center.faq1_answer")
        val Faq2Question = StringKey("help_center.faq2_question")
        val Faq2Answer = StringKey("help_center.faq2_answer")
        val Faq3Question = StringKey("help_center.faq3_question")
        val Faq3Answer = StringKey("help_center.faq3_answer")
        val Faq4Question = StringKey("help_center.faq4_question")
        val Faq4Answer = StringKey("help_center.faq4_answer")
    }
    
    // ========================================
    // ÉCRAN CONTACTER LE SUPPORT
    // ========================================
    object Contact {
        val Title = StringKey("contact.title")
        val ResponseTime = StringKey("contact.response_time")
        val SendMessage = StringKey("contact.send_message")
        val Subject = StringKey("contact.subject")
        val Message = StringKey("contact.message")
        val Send = StringKey("contact.send")
        val Sending = StringKey("contact.sending")
    }
    
    // ========================================
    // ÉCRAN CONDITIONS D'UTILISATION
    // ========================================
    object Terms {
        val Title = StringKey("terms.title")
        val LastUpdate = StringKey("terms.last_update")
        val Section1Title = StringKey("terms.section1_title")
        val Section1Content = StringKey("terms.section1_content")
        val Section2Title = StringKey("terms.section2_title")
        val Section2Content = StringKey("terms.section2_content")
        val Section3Title = StringKey("terms.section3_title")
        val Section3Content = StringKey("terms.section3_content")
        val Section4Title = StringKey("terms.section4_title")
        val Section4Content = StringKey("terms.section4_content")
    }
    
    // ========================================
    // ÉCRAN POLITIQUE DE CONFIDENTIALITÉ
    // ========================================
    object Privacy {
        val Title = StringKey("privacy.title")
        val LastUpdate = StringKey("privacy.last_update")
        val Section1Title = StringKey("privacy.section1_title")
        val Section1Content = StringKey("privacy.section1_content")
        val Section2Title = StringKey("privacy.section2_title")
        val Section2Content = StringKey("privacy.section2_content")
        val Section3Title = StringKey("privacy.section3_title")
        val Section3Content = StringKey("privacy.section3_content")
        val Section4Title = StringKey("privacy.section4_title")
        val Section4Content = StringKey("privacy.section4_content")
    }
    
    // ========================================
    // ÉCRAN DE PAIEMENT
    // ========================================
    object Payment {
        val Title = StringKey("payment.title")
        val Cancel = StringKey("payment.cancel")
        val Service = StringKey("payment.service")
        val Salon = StringKey("payment.salon")
        val Date = StringKey("payment.date")
        
        // PaymentFlowScreen
        val CardInput = StringKey("payment.card_input")
        val Confirmation = StringKey("payment.confirmation")
        val Processing = StringKey("payment.processing")
        val Success = StringKey("payment.success")
        val Error = StringKey("payment.error")
        val Continue = StringKey("payment.continue")
        val Retry = StringKey("payment.retry")
        val PayAmount = StringKey("payment.pay_amount")
        val VerifyOrder = StringKey("payment.verify_order")
        val BookingDetails = StringKey("payment.booking_details")
        val Card = StringKey("payment.card")
        val TotalToPay = StringKey("payment.total_to_pay")
        val ModifyCard = StringKey("payment.modify_card")
        val ProcessingMessage = StringKey("payment.processing_message")
        val SuccessMessage = StringKey("payment.success_message")
        val ErrorMessage = StringKey("payment.error_message")
        val ViewBooking = StringKey("payment.view_booking")
        
        // Card input fields
        val CardNumber = StringKey("payment.card_number")
        val CardHolder = StringKey("payment.card_holder")
        val Expiry = StringKey("payment.expiry")
        val Cvv = StringKey("payment.cvv")
        
        // Security badges
        val Ssl = StringKey("payment.ssl")
        val PciDss = StringKey("payment.pci_dss")
        val ThreeDSecure = StringKey("payment.3d_secure")
        
        // Payment history
        val History = StringKey("payment.history")
        val NoPayments = StringKey("payment.no_payments")
        val Summary = StringKey("payment.summary")
        val TotalSpent = StringKey("payment.total_spent")
        val Transactions = StringKey("payment.transactions")
        val SuccessRate = StringKey("payment.success_rate")
        val AllPayments = StringKey("payment.all_payments")
        val Succeeded = StringKey("payment.succeeded")
        val Failed = StringKey("payment.failed")
        val Refunded = StringKey("payment.refunded")
        val TransactionId = StringKey("payment.transaction_id")
        val StripeReference = StringKey("payment.stripe_reference")
        val PaymentMethod = StringKey("payment.payment_method")
        val Currency = StringKey("payment.currency")
        val RefundedAmount = StringKey("payment.refunded_amount")
        
        // Stripe Checkout
        val Redirect = StringKey("payment.redirect")
        val SecurePayment = StringKey("payment.secure_payment")
        val SecurePaymentDescription = StringKey("payment.secure_payment_description")
        val OrderSummary = StringKey("payment.order_summary")
        val Total = StringKey("payment.total")
        val Loading = StringKey("payment.loading")
        val ProceedToPayment = StringKey("payment.proceed_to_payment")
        val SecureSSL = StringKey("payment.secure_ssl")
        val StripeSecure = StringKey("payment.stripe_secure")
        val PciCompliant = StringKey("payment.pci_compliant")
        val RedirectToStripe = StringKey("payment.redirect_to_stripe")
        val RedirectDescription = StringKey("payment.redirect_description")
        val OpenPaymentPage = StringKey("payment.open_payment_page")
        val CheckPaymentStatus = StringKey("payment.check_payment_status")
        val ProcessingPayment = StringKey("payment.processing_payment")
        val ProcessingDescription = StringKey("payment.processing_description")
        val PaymentSuccessful = StringKey("payment.payment_successful")
        val PaymentSuccessDescription = StringKey("payment.payment_success_description")
        val PaymentFailed = StringKey("payment.payment_failed")
        val PaymentFailedDescription = StringKey("payment.payment_failed_description")
        
        // Steps
        val StepSummary = StringKey("payment.step_summary")
        val StepPayment = StringKey("payment.step_payment")
        val StepConfirmation = StringKey("payment.step_confirmation")
    }
    
    // ========================================
    // ÉCRAN DE DEMANDE DE VÉRIFICATION
    // ========================================
    object RequestVerification {
        val Title = StringKey("request_verification.title")
        val HeaderTitle = StringKey("request_verification.header_title")
        val Description = StringKey("request_verification.description")
        val VerificationType = StringKey("request_verification.verification_type")
        val AdditionalInfo = StringKey("request_verification.additional_info")
        val AdditionalInfoPlaceholder = StringKey("request_verification.additional_info_placeholder")
    }
    
    // ========================================
    // ÉCRAN DE CRÉATION D'AVIS
    // ========================================
    object CreateReview {
        val Title = StringKey("create_review.title")
        val YourBooking = StringKey("create_review.your_booking")
        val Salon = StringKey("create_review.salon")
        val Date = StringKey("create_review.date")
        val RatingQuestion = StringKey("create_review.rating_question")
        val TitleLabel = StringKey("create_review.title_label")
        val TitlePlaceholder = StringKey("create_review.title_placeholder")
        val CommentLabel = StringKey("create_review.comment_label")
        val CommentPlaceholder = StringKey("create_review.comment_placeholder")
    }
    
    // ========================================
    // ÉCRAN DE GESTION DES RENDEZ-VOUS PROPRIÉTAIRE
    // ========================================
    object OwnerBookingsManagement {
        val Title = StringKey("owner_bookings_management.title")
        val Confirm = StringKey("owner_bookings_management.confirm")
        val Start = StringKey("owner_bookings_management.start")
        val Absent = StringKey("owner_bookings_management.absent")
        val Finish = StringKey("owner_bookings_management.finish")
    }
    
    // ========================================
    // ÉCRAN DE RECHERCHE
    // ========================================
    object Search {
        val Title = StringKey("search.title")
        val AdvancedFilters = StringKey("search.advanced_filters")
        val PostType = StringKey("search.post_type")
        val SearchPlaceholder = StringKey("search.search_placeholder")
        val NoPostsFound = StringKey("search.no_posts_found")
        val NoSalonsFound = StringKey("search.no_salons_found")
        val NoUsersFound = StringKey("search.no_users_found")
        val NoHashtagsFound = StringKey("search.no_hashtags_found")
        val NoResultsFound = StringKey("search.no_results_found")
        val Posts = StringKey("search.posts")
        val Salons = StringKey("search.salons")
        val Users = StringKey("search.users")
        val Hashtags = StringKey("search.hashtags")
        val All = StringKey("search.all")
        val LoadMore = StringKey("search.load_more")
    }
    
    // ========================================
    // COMPOSANT ULTRA PREMIUM POST CARD
    // ========================================
    object UltraPremiumPostCard {
        val Oops = StringKey("ultra_premium_post_card.oops")
        val Retry = StringKey("ultra_premium_post_card.retry")
        val NoPostsYet = StringKey("ultra_premium_post_card.no_posts_yet")
        val BeFirstToShare = StringKey("ultra_premium_post_card.be_first_to_share")
        val ViewComments = StringKey("ultra_premium_post_card.view_comments") // Utilisé avec pluralizedString
        val AddComment = StringKey("ultra_premium_post_card.add_comment")
        val Pinned = StringKey("ultra_premium_post_card.pinned")
    }
    
    // ========================================
    // COMPOSANTS UI
    // ========================================
    object Components {
        // RatingBar
        object RatingBar {
            val StarFilled = StringKey("components.rating_bar.star_filled")
            val StarEmpty = StringKey("components.rating_bar.star_empty")
            val ReviewsCount = StringKey("components.rating_bar.reviews_count")
        }
        
        // PasswordTextField
        object PasswordTextField {
            val Label = StringKey("components.password_text_field.label")
            val Placeholder = StringKey("components.password_text_field.placeholder")
            val ShowPassword = StringKey("components.password_text_field.show_password")
            val HidePassword = StringKey("components.password_text_field.hide_password")
        }
        
        // ReportDialog
        object ReportDialog {
            val Title = StringKey("components.report_dialog.title")
            val InfoMessage = StringKey("components.report_dialog.info_message")
            val ReasonLabel = StringKey("components.report_dialog.reason_label")
            val AdditionalInfoLabel = StringKey("components.report_dialog.additional_info_label")
            val AdditionalInfoPlaceholder = StringKey("components.report_dialog.additional_info_placeholder")
            val ErrorSelectReason = StringKey("components.report_dialog.error_select_reason")
            val ErrorReporting = StringKey("components.report_dialog.error_reporting")
            val ErrorUnknown = StringKey("components.report_dialog.error_unknown")
        }
        
        // UserAvatar
        object UserAvatar {
            val ContentDescription = StringKey("components.user_avatar.content_description")
        }
        
        // QueueStatusCard
        object QueueStatusCard {
            val ConnectionLost = StringKey("components.queue_status_card.connection_lost")
            val DataStale = StringKey("components.queue_status_card.data_stale")
            val LastUpdate = StringKey("components.queue_status_card.last_update")
            val YourProgress = StringKey("components.queue_status_card.your_progress")
            val CurrentPosition = StringKey("components.queue_status_card.current_position")
            val EstimatedTime = StringKey("components.queue_status_card.estimated_time")
            val JustNow = StringKey("components.queue_status_card.just_now")
            val MinutesAgo = StringKey("components.queue_status_card.minutes_ago")
            val LeaveQueue = StringKey("components.queue_status_card.leave_queue")
            val Reconnecting = StringKey("components.queue_status_card.reconnecting")
            val KeepAppOpen = StringKey("components.queue_status_card.keep_app_open")
            val StatusOffline = StringKey("components.queue_status_card.status_offline")
            val StatusPending = StringKey("components.queue_status_card.status_pending")
            val StatusAutoRefresh = StringKey("components.queue_status_card.status_auto_refresh")
        }
        
        // FullScreenImageViewer
        object FullScreenImageViewer {
            val ImageContentDescription = StringKey("components.full_screen_image_viewer.image_content_description")
            val Close = StringKey("components.full_screen_image_viewer.close")
        }
        
        // AppDrawer
        object AppDrawer {
            val Marketplace = StringKey("components.app_drawer.marketplace")
            val Social = StringKey("components.app_drawer.social")
            val Appointments = StringKey("components.app_drawer.appointments")
            val Account = StringKey("components.app_drawer.account")
            val Profile = StringKey("components.app_drawer.profile")
            val Notifications = StringKey("components.app_drawer.notifications")
            val Favorites = StringKey("components.app_drawer.favorites")
            val Archives = StringKey("components.app_drawer.archives")
            val Collections = StringKey("components.app_drawer.collections")
            val Management = StringKey("components.app_drawer.management")
            val MySalons = StringKey("components.app_drawer.my_salons")
            val NewSalon = StringKey("components.app_drawer.new_salon")
            val CreatePost = StringKey("components.app_drawer.create_post")
            val BookingsManagement = StringKey("components.app_drawer.bookings_management")
            val Stats = StringKey("components.app_drawer.stats")
            val Activity = StringKey("components.app_drawer.activity")
            val MyPortfolios = StringKey("components.app_drawer.my_portfolios")
            val NewPortfolio = StringKey("components.app_drawer.new_portfolio")
            val Services = StringKey("components.app_drawer.services")
            val Agenda = StringKey("components.app_drawer.agenda")
            val Admin = StringKey("components.app_drawer.admin")
            val Dashboard = StringKey("components.app_drawer.dashboard")
            val Users = StringKey("components.app_drawer.users")
            val Settings = StringKey("components.app_drawer.settings")
            val Help = StringKey("components.app_drawer.help")
            val Guest = StringKey("components.app_drawer.guest")
            val Client = StringKey("components.app_drawer.client")
            val Owner = StringKey("components.app_drawer.owner")
            val Hairstylist = StringKey("components.app_drawer.hairstylist")
            val AdminUser = StringKey("components.app_drawer.admin_user")
        }
        
        // ExternalShareDialog
        object ExternalShareDialog {
            val Title = StringKey("components.external_share_dialog.title")
            val ShareViaApp = StringKey("components.external_share_dialog.share_via_app")
            val ShareViaAppDescription = StringKey("components.external_share_dialog.share_via_app_description")
            val ErrorSharing = StringKey("components.external_share_dialog.error_sharing")
            val CopyLink = StringKey("components.external_share_dialog.copy_link")
            val CopyLinkDescription = StringKey("components.external_share_dialog.copy_link_description")
            val ErrorCopying = StringKey("components.external_share_dialog.error_copying")
            val NotAvailable = StringKey("components.external_share_dialog.not_available")
        }
        
        // SearchTextField
        object SearchTextField {
            val Placeholder = StringKey("components.search_text_field.placeholder")
            val ContentDescription = StringKey("components.search_text_field.content_description")
        }
    }
    
    // ========================================
    // ENUMS - LOCALISATION
    // ========================================
    object Enums {
        // BookingStatus
        object BookingStatus {
            val Pending = StringKey("enums.booking_status.pending")
            val Confirmed = StringKey("enums.booking_status.confirmed")
            val InProgress = StringKey("enums.booking_status.in_progress")
            val Completed = StringKey("enums.booking_status.completed")
            val Cancelled = StringKey("enums.booking_status.cancelled")
            val NoShow = StringKey("enums.booking_status.no_show")
        }
        
        // PaymentStatus
        object PaymentStatus {
            val Pending = StringKey("enums.payment_status.pending")
            val Processing = StringKey("enums.payment_status.processing")
            val Succeeded = StringKey("enums.payment_status.succeeded")
            val Failed = StringKey("enums.payment_status.failed")
            val Canceled = StringKey("enums.payment_status.canceled")
            val PartiallyRefunded = StringKey("enums.payment_status.partially_refunded")
            val Unpaid = StringKey("enums.payment_status.unpaid")
            val Paid = StringKey("enums.payment_status.paid")
            val Refunded = StringKey("enums.payment_status.refunded")
        }
        
        // PostType
        object PostType {
            val General = StringKey("enums.post_type.general")
            val AvantApres = StringKey("enums.post_type.avant_apres")
            val Portfolio = StringKey("enums.post_type.portfolio")
            val Tendance = StringKey("enums.post_type.tendance")
            val Conseil = StringKey("enums.post_type.conseil")
            val Realisation = StringKey("enums.post_type.realisation")
            val Inspiration = StringKey("enums.post_type.inspiration")
            
            // Descriptions
            val GeneralDescription = StringKey("enums.post_type.general_description")
            val AvantApresDescription = StringKey("enums.post_type.avant_apres_description")
            val PortfolioDescription = StringKey("enums.post_type.portfolio_description")
            val TendanceDescription = StringKey("enums.post_type.tendance_description")
            val ConseilDescription = StringKey("enums.post_type.conseil_description")
            val RealisationDescription = StringKey("enums.post_type.realisation_description")
            val InspirationDescription = StringKey("enums.post_type.inspiration_description")
        }
        
        // PostVisibility
        object PostVisibility {
            val Public = StringKey("enums.post_visibility.public")
            val Followers = StringKey("enums.post_visibility.followers")
            val Private = StringKey("enums.post_visibility.private")
            
            // Descriptions
            val PublicDescription = StringKey("enums.post_visibility.public_description")
            val FollowersDescription = StringKey("enums.post_visibility.followers_description")
            val PrivateDescription = StringKey("enums.post_visibility.private_description")
        }
        
        // ServiceCategory
        object ServiceCategory {
            val Coupe = StringKey("enums.service_category.coupe")
            val Coloration = StringKey("enums.service_category.coloration")
            val Soin = StringKey("enums.service_category.soin")
            val Coiffage = StringKey("enums.service_category.coiffage")
            val Barbe = StringKey("enums.service_category.barbe")
            val Technique = StringKey("enums.service_category.technique")
            val Autre = StringKey("enums.service_category.autre")
        }
        
        // ReactionType
        object ReactionType {
            val Like = StringKey("enums.reaction_type.like")
            val Love = StringKey("enums.reaction_type.love")
            val Wow = StringKey("enums.reaction_type.wow")
            val Inspirant = StringKey("enums.reaction_type.inspirant")
            val Magnifique = StringKey("enums.reaction_type.magnifique")
            val Bravo = StringKey("enums.reaction_type.bravo")
            
            // Descriptions
            val LikeDescription = StringKey("enums.reaction_type.like_description")
            val LoveDescription = StringKey("enums.reaction_type.love_description")
            val WowDescription = StringKey("enums.reaction_type.wow_description")
            val InspirantDescription = StringKey("enums.reaction_type.inspirant_description")
            val MagnifiqueDescription = StringKey("enums.reaction_type.magnifique_description")
            val BravoDescription = StringKey("enums.reaction_type.bravo_description")
        }
        
        // MediaType
        object MediaType {
            val Before = StringKey("enums.media_type.before")
            val After = StringKey("enums.media_type.after")
            val Process = StringKey("enums.media_type.process")
            val Detail = StringKey("enums.media_type.detail")
        }
        
        // ReportReason
        object ReportReason {
            val Inapproprie = StringKey("enums.report_reason.inapproprie")
            val Spam = StringKey("enums.report_reason.spam")
            val Faux = StringKey("enums.report_reason.faux")
            val Copyright = StringKey("enums.report_reason.copyright")
            val Autre = StringKey("enums.report_reason.autre")
            
            // Descriptions
            val InapproprieDescription = StringKey("enums.report_reason.inapproprie_description")
            val SpamDescription = StringKey("enums.report_reason.spam_description")
            val FauxDescription = StringKey("enums.report_reason.faux_description")
            val CopyrightDescription = StringKey("enums.report_reason.copyright_description")
            val AutreDescription = StringKey("enums.report_reason.autre_description")
        }
        
        // ReportedEntityType
        object ReportedEntityType {
            val Post = StringKey("enums.reported_entity_type.post")
            val Comment = StringKey("enums.reported_entity_type.comment")
            val User = StringKey("enums.reported_entity_type.user")
            val Salon = StringKey("enums.reported_entity_type.salon")
        }
        
        // VerificationType
        object VerificationType {
            val Email = StringKey("enums.verification_type.email")
            val Phone = StringKey("enums.verification_type.phone")
            val Business = StringKey("enums.verification_type.business")
            val Professional = StringKey("enums.verification_type.professional")
            
            // Descriptions
            val EmailDescription = StringKey("enums.verification_type.email_description")
            val PhoneDescription = StringKey("enums.verification_type.phone_description")
            val BusinessDescription = StringKey("enums.verification_type.business_description")
            val ProfessionalDescription = StringKey("enums.verification_type.professional_description")
        }
        
        // BadgeCategory
        object BadgeCategory {
            val Certification = StringKey("enums.badge_category.certification")
            val Competition = StringKey("enums.badge_category.competition")
            val Formation = StringKey("enums.badge_category.formation")
            val Partenariat = StringKey("enums.badge_category.partenariat")
        }
        
        // ReportStatus
        object ReportStatus {
            val Pending = StringKey("enums.report_status.pending")
            val Reviewed = StringKey("enums.report_status.reviewed")
            val Resolved = StringKey("enums.report_status.resolved")
            val Dismissed = StringKey("enums.report_status.dismissed")
        }
        
        // ModerationAction
        object ModerationAction {
            val Hide = StringKey("enums.moderation_action.hide")
            val Delete = StringKey("enums.moderation_action.delete")
            val Warn = StringKey("enums.moderation_action.warn")
            
            // Descriptions
            val HideDescription = StringKey("enums.moderation_action.hide_description")
            val DeleteDescription = StringKey("enums.moderation_action.delete_description")
            val WarnDescription = StringKey("enums.moderation_action.warn_description")
        }

        // AppealStatus
        object AppealStatus {
            val None = StringKey("enums.appeal_status.none")
            val Pending = StringKey("enums.appeal_status.pending")
            val Approved = StringKey("enums.appeal_status.approved")
            val Rejected = StringKey("enums.appeal_status.rejected")
        }

        // HairHashtagCategory
        object HairHashtagCategory {
            val Technique = StringKey("enums.hair_hashtag_category.technique")
            val Style = StringKey("enums.hair_hashtag_category.style")
            val Couleur = StringKey("enums.hair_hashtag_category.couleur")
            val Longueur = StringKey("enums.hair_hashtag_category.longueur")
            val Texture = StringKey("enums.hair_hashtag_category.texture")
        }
        
        // FollowingType
        object FollowingType {
            val Coiffeur = StringKey("enums.following_type.coiffeur")
            val Salon = StringKey("enums.following_type.salon")
        }
    }
    
    // ========================================
    // BOUTONS ET ACTIONS COMMUNS
    // ========================================
    object Common {
        val Cancel = StringKey("common.cancel")
        val Save = StringKey("common.save")
        val Confirm = StringKey("common.confirm")
        val Retry = StringKey("common.retry")
        val Delete = StringKey("common.delete")
        val Edit = StringKey("common.edit")
        val Remove = StringKey("common.remove")
        val Add = StringKey("common.add")
        val Create = StringKey("common.create")
        val LoadMore = StringKey("common.load_more")
        val All = StringKey("common.all")
        val Start = StringKey("common.start")
        val Finish = StringKey("common.finish")
        val Absent = StringKey("common.absent")
        val Popular = StringKey("common.popular")
        val Public = StringKey("common.public")
        val Service = StringKey("common.service")
        val Salon = StringKey("common.salon")
        val Date = StringKey("common.date")
        val Verified = StringKey("common.verified")
    }
    
    // Les autres structures seront ajoutées progressivement lors des migrations suivantes
}


