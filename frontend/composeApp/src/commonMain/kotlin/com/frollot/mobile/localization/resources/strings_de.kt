package com.frollot.mobile.localization.resources

/**
 * Bundle deutscher Strings.
 * 
 * Vollständige Übersetzungen für alle Bildschirme und Komponenten der Anwendung.
 */
fun createGermanStrings(): StringsBundle {
    val strings = mapOf<String, String>(
        // ========================================
        // ANMELDEBILDSCHIRM
        // ========================================
        "login.welcome_title" to "Willkommen 👋",
        "login.welcome_subtitle" to "Melden Sie sich an und erkunden Sie\nunendliche Möglichkeiten",
        "login.email_label" to "E-Mail-Adresse",
        "login.email_placeholder" to "sie@beispiel.com",
        "login.password_label" to "Passwort",
        "login.password_placeholder" to "••••••••",
        "login.forgot_password" to "Passwort vergessen?",
        "login.submit_button" to "Anmelden",
        "login.submit_button_loading" to "Anmeldung läuft...",
        "login.continue_with" to "ODER WEITER MIT",
        "login.google_button" to "Google",
        "login.facebook_button" to "Facebook",
        "login.new_here" to "Neu hier?",
        "login.create_account" to "Konto erstellen",
        "login.secure_connection" to "100% sichere Verbindung",
        
        // Anmeldefehlermeldungen
        "login.errors.invalid_credentials" to "Falsche E-Mail oder falsches Passwort",
        "login.errors.account_disabled" to "Konto deaktiviert. Bitte kontaktieren Sie den Support.",
        "login.errors.account_not_found" to "Konto nicht gefunden",
        "login.errors.server_unavailable" to "Server nicht erreichbar",
        "login.errors.timeout" to "Zeitüberschreitung. Überprüfen Sie Ihre Internetverbindung.",
        "login.errors.generic_error" to "Fehler bei der Anmeldung",
        
        // ========================================
        // REGISTRIERUNGSBILDSCHIRM
        // ========================================
        "register.welcome_title" to "Werden Sie Teil von uns 🎉",
        "register.welcome_subtitle" to "Erstellen Sie Ihr Konto in Sekunden",
        "register.first_name_label" to "Vorname",
        "register.first_name_placeholder" to "Sophie",
        "register.last_name_label" to "Nachname",
        "register.last_name_placeholder" to "Martin",
        "register.email_label" to "E-Mail-Adresse",
        "register.email_placeholder" to "sie@beispiel.com",
        "register.account_type_label" to "Kontotyp",
        "register.password_label" to "Passwort",
        "register.password_placeholder" to "Mindestens 8 Zeichen",
        "register.confirm_password_label" to "Passwort bestätigen",
        "register.confirm_password_placeholder" to "Geben Sie Ihr Passwort erneut ein",
        "register.password_mismatch" to "Die Passwörter stimmen nicht überein",
        "register.submit_button" to "Mein Konto erstellen",
        "register.submit_button_loading" to "Konto wird erstellt...",
        "register.sign_up_with" to "ODER REGISTRIEREN SIE SICH MIT",
        "register.google_button" to "Google",
        "register.facebook_button" to "Facebook",
        "register.already_registered" to "Bereits registriert?",
        "register.login_link" to "Anmelden",
        "register.data_protected" to "Ihre Daten sind geschützt",
        "register.success_message" to "Willkommen {firstName} {lastName}! Ihr Konto wurde erfolgreich erstellt.",
        
        // Kontotypen
        "register.user_types.client" to "👤 Kunde",
        "register.user_types.hairstylist" to "✂️ Friseur",
        "register.user_types.salon_owner" to "🏢 Salonbesitzer",
        "register.user_types.admin" to "⚙️ Administrator",
        
        // Registrierungsfehlermeldungen
        "register.errors.email_already_used" to "Diese E-Mail wird bereits verwendet",
        "register.errors.invalid_data" to "Ungültige Daten, überprüfen Sie Ihre Informationen",
        "register.errors.server_unavailable" to "Server nicht erreichbar",
        "register.errors.timeout" to "Zeitüberschreitung. Überprüfen Sie Ihre Internetverbindung.",
        "register.errors.generic_error" to "Fehler bei der Registrierung",
        
        // ========================================
        // STARTBILDSCHIRM
        // ========================================
        "home.title" to "Frollot",
        "home.filters_button" to "Filter",
        "home.my_bookings_button" to "Meine Buchungen",
        "home.create_salon_button" to "Einen Salon erstellen",
        "home.loading_message" to "Salons werden geladen",
        "home.error_message" to "Salons können nicht geladen werden",
        "home.empty_state_title" to "Keine Salons verfügbar",
        "home.empty_state_title_with_filters" to "Keine Salons entsprechen Ihren Kriterien",
        "home.empty_state_message" to "Seien Sie der Erste, der einen Salon erstellt!",
        "home.empty_state_message_with_filters" to "Versuchen Sie, Ihre Suche zu erweitern oder einige Filter zu entfernen.",
        "home.create_my_salon" to "Meinen Salon erstellen",
        "home.premium_salons" to "Premium Salons",
        "home.near_me" to "In meiner Nähe",
        "home.city" to "Stadt",
        "home.salons_found" to "{count} Salon gefunden",
        "home.salons_found_plural" to "{count} Salons gefunden",
        "home.filter_by_city" to "Nach Stadt filtern",
        "home.filter_by_city_description" to "Geben Sie eine Stadt ein, um die Salons zu filtern.",
        "home.city_placeholder" to "z.B. Berlin",
        "home.apply" to "Anwenden",
        "home.reset" to "Zurücksetzen",
        "home.premium_badge" to "PREMIUM",
        "home.rating" to "Bewertung",
        "home.open_hours" to "Geöffnet • 9-19 Uhr",
        "home.cut" to "Schnitt",
        "home.coloring" to "Färbung",
        "home.book" to "Buchen",
        "home.cover_photo_description" to "Titelbild von {salonName}",
        
        // ========================================
        // EINSTELLUNGSBILDSCHIRM
        // ========================================
        "settings.title" to "Einstellungen",
        
        // Abschnitte
        "settings.sections.account" to "Konto",
        "settings.sections.privacy" to "Datenschutz",
        "settings.sections.notifications" to "Benachrichtigungen",
        "settings.sections.appearance" to "Erscheinungsbild",
        "settings.sections.social_network" to "Soziales Netzwerk",
        "settings.sections.bookings" to "Buchungen",
        "settings.sections.content_and_media" to "Inhalt & Medien",
        "settings.sections.data_privacy" to "Datenschutz",
        "settings.sections.help_and_support" to "Hilfe & Support",
        "settings.sections.about" to "Über",
        
        // Konto
        "settings.account.profile" to "Profil",
        "settings.account.profile_subtitle" to "Verwalten Sie Ihr Profil und Ihre Informationen",
        "settings.account.security" to "Sicherheit",
        "settings.account.security_subtitle" to "Passwort, Zwei-Faktor-Authentifizierung",
        "settings.account.email" to "E-Mail",
        "settings.account.phone" to "Telefon",
        "settings.account.phone_subtitle" to "Telefonnummer hinzufügen",
        "settings.account.not_defined" to "Nicht definiert",
        
        // Datenschutz
        "settings.privacy.profile_visibility" to "Profil-Sichtbarkeit",
        "settings.privacy.who_can_follow_me" to "Wer kann mir folgen",
        "settings.privacy.who_can_message_me" to "Wer kann mir Nachrichten senden",
        "settings.privacy.show_activity_status" to "Aktivitätsstatus anzeigen",
        "settings.privacy.show_activity_status_subtitle" to "Andere Benutzer sehen, wenn Sie online sind",
        "settings.privacy.blocked_users" to "Blockierte Benutzer",
        "settings.privacy.blocked_users_count" to "{count} Benutzer",
        "settings.privacy.no_blocked_users" to "Keine blockierten Benutzer",
        "settings.privacy.options.public" to "Öffentlich",
        "settings.privacy.options.followers_only" to "Nur Follower",
        "settings.privacy.options.private" to "Privat",
        "settings.privacy.options.everyone" to "Jeder",
        "settings.privacy.options.nobody" to "Niemand",
        
        // Benachrichtigungen
        "settings.notifications.title" to "Benachrichtigungen",
        "settings.notifications.subtitle" to "Alle Benachrichtigungen aktivieren oder deaktivieren",
        "settings.notifications.push" to "Push-Benachrichtigungen",
        "settings.notifications.push_subtitle" to "Benachrichtigungen auf Ihrem Gerät erhalten",
        "settings.notifications.email" to "E-Mail-Benachrichtigungen",
        "settings.notifications.email_subtitle" to "Benachrichtigungen per E-Mail erhalten",
        "settings.notifications.bookings" to "Buchungen",
        "settings.notifications.bookings_subtitle" to "Benachrichtigungen für Ihre Termine",
        "settings.notifications.social" to "Soziales Netzwerk",
        "settings.notifications.social_subtitle" to "Likes, Kommentare, Erwähnungen usw.",
        "settings.notifications.marketing" to "Marketing",
        "settings.notifications.marketing_subtitle" to "Sonderangebote und Neuigkeiten",
        
        // Erscheinungsbild
        "settings.appearance.dark_mode" to "Dunkler Modus",
        "settings.appearance.dark_mode_subtitle" to "Dunkles Design aktivieren",
        "settings.appearance.language" to "Sprache",
        
        // Soziales Netzwerk
        "settings.social_network.post_visibility_default" to "Standard-Sichtbarkeit von Beiträgen",
        "settings.social_network.allow_comments" to "Kommentare zulassen",
        "settings.social_network.allow_comments_subtitle" to "Andere Benutzer können Ihre Beiträge kommentieren",
        "settings.social_network.allow_reactions" to "Reaktionen zulassen",
        "settings.social_network.allow_reactions_subtitle" to "Andere Benutzer können auf Ihre Beiträge reagieren",
        "settings.social_network.allow_shares" to "Teilen zulassen",
        "settings.social_network.allow_shares_subtitle" to "Andere Benutzer können Ihre Beiträge teilen",
        
        // Buchungen
        "settings.bookings.booking_notifications" to "Buchungsbenachrichtigungen",
        "settings.bookings.booking_notifications_subtitle" to "Benachrichtigungen für Ihre Termine erhalten",
        "settings.bookings.availability_preferences" to "Verfügbarkeitspräferenzen",
        "settings.bookings.availability_preferences_subtitle" to "Verwalten Sie Ihre bevorzugten Zeitslots",
        "settings.bookings.payment_methods" to "Zahlungsmethoden",
        "settings.bookings.payment_methods_subtitle" to "Verwalten Sie Ihre Zahlungsmethoden",
        
        // Inhalt und Medien
        "settings.content_and_media.auto_save_photos" to "Fotos automatisch speichern",
        "settings.content_and_media.auto_save_photos_subtitle" to "Fotos in Ihrer Galerie speichern",
        "settings.content_and_media.data_usage" to "Datenverbrauch",
        "settings.content_and_media.video_quality" to "Videoqualität",
        "settings.content_and_media.data_usage_options.economical" to "Wirtschaftlich",
        "settings.content_and_media.data_usage_options.standard" to "Standard",
        "settings.content_and_media.data_usage_options.high" to "Hoch",
        "settings.content_and_media.video_quality_options.sd" to "SD",
        "settings.content_and_media.video_quality_options.hd" to "HD",
        "settings.content_and_media.video_quality_options.full_hd" to "Full HD",
        
        // Datenschutz
        "settings.data_privacy.download_data" to "Ihre Daten herunterladen",
        "settings.data_privacy.download_data_subtitle" to "Eine Kopie Ihrer Daten erhalten",
        "settings.data_privacy.delete_account" to "Ihr Konto löschen",
        "settings.data_privacy.delete_account_subtitle" to "Ihr Konto und alle Ihre Daten dauerhaft löschen",
        
        // Hilfe und Support
        "settings.help_and_support.help_center" to "Hilfezentrum",
        "settings.help_and_support.help_center_subtitle" to "FAQ und Nutzungsanleitungen",
        "settings.help_and_support.contact_us" to "Kontaktieren Sie uns",
        "settings.help_and_support.contact_us_subtitle" to "Ein Problem melden oder eine Frage stellen",
        "settings.help_and_support.report_bug" to "Fehler melden",
        "settings.help_and_support.report_bug_subtitle" to "Helfen Sie uns, die App zu verbessern",
        "settings.help_and_support.rate_app" to "App bewerten",
        "settings.help_and_support.rate_app_subtitle" to "Teilen Sie Ihr Feedback im Store",
        
        // Über
        "settings.about.version" to "Version",
        "settings.about.terms_of_service" to "Nutzungsbedingungen",
        "settings.about.terms_of_service_subtitle" to "Nutzungsbedingungen lesen",
        "settings.about.privacy_policy" to "Datenschutzrichtlinie",
        "settings.about.privacy_policy_subtitle" to "Datenschutzrichtlinie lesen",
        "settings.about.licenses" to "Lizenzen",
        "settings.about.licenses_subtitle" to "Open-Source-Lizenzen",
        
        // Aktionen
        "settings.actions.logout" to "Abmelden",
        "settings.actions.logout_dialog_title" to "Abmelden",
        "settings.actions.logout_dialog_text" to "Sind Sie sicher, dass Sie sich abmelden möchten?",
        "settings.actions.delete" to "Löschen",
        "settings.actions.delete_account_dialog_title" to "Ihr Konto löschen",
        "settings.actions.delete_account_dialog_text" to "Diese Aktion ist unwiderruflich. Alle Ihre Daten werden dauerhaft gelöscht. Sind Sie sich absolut sicher?",
        "settings.actions.cancel" to "Abbrechen",
        
        // ========================================
        // PROFILBILDSCHIRM
        // ========================================
        "profile.version" to "Version 1.0.0",
        "profile.logout_dialog_title" to "Abmelden",
        "profile.logout_dialog_text" to "Sind Sie sicher, dass Sie sich abmelden möchten?",
        "profile.logout_confirm" to "Ja, abmelden",
        "profile.cancel" to "Abbrechen",
        "profile.avatar_preview" to "Avatar-Vorschau",
        "profile.save" to "Speichern",
        "profile.edit_photo" to "Foto bearbeiten",
        "profile.account_verified" to "Verifiziertes Konto",
        "profile.account_not_verified" to "Nicht verifiziert",
        "profile.account_info" to "Kontoinformationen",
        "profile.email" to "E-Mail",
        "profile.phone" to "Telefon",
        "profile.member_since" to "Mitglied seit",
        "profile.account_type" to "Kontotyp",
        "profile.edit_profile" to "Mein Profil bearbeiten",
        "profile.edit_profile_subtitle" to "Name, Vorname, Telefon ändern",
        "profile.change_password" to "Passwort ändern",
        "profile.change_password_subtitle" to "Ihre Anmeldedaten aktualisieren",
        "profile.logout" to "Abmelden",
        "profile.logout_subtitle" to "Ihre Sitzung beenden",
        "profile.statistics" to "Statistiken",
        "profile.salons" to "Salons",
        "profile.bookings" to "Buchungen",
        "profile.reviews" to "Bewertungen",
        "profile.services" to "Dienstleistungen",
        "profile.points" to "Punkte",
        "profile.user_types.client" to "Kunde",
        "profile.user_types.salon_owner" to "Salonbesitzer",
        "profile.user_types.hairstylist" to "Friseur",
        "profile.user_types.admin" to "Administrator",
        
        // ========================================
        // SOZIALER FEED-BILDSCHIRM
        // ========================================
        "social_feed.filter_by_type" to "Nach Typ filtern",
        "social_feed.my_follows" to "Meine Follower",
        "social_feed.near_me" to "In meiner Nähe",
        "social_feed.all" to "Alle",
        "social_feed.add_comment" to "Einen Kommentar hinzufügen (optional)",
        "social_feed.share" to "Teilen",
        "social_feed.cancel" to "Abbrechen",
        "social_feed.no_collection_yet" to "Sie haben noch keine Sammlung",
        "social_feed.save" to "Speichern",
        "social_feed.archive" to "Archivieren",
        "social_feed.share_in_app" to "In der App teilen",
        "social_feed.share_external" to "Extern teilen",
        "social_feed.report" to "Melden",
        "social_feed.add_to_collection_example" to "Z.B.: Ich habe in diesem Salon dank dieses Posts gebucht!",
        "social_feed.add_to_collection" to "Zur Sammlung hinzufügen",
        "social_feed.create_collection" to "Sammlung erstellen",
        
        // ========================================
        // MEINE BUCHUNGEN BILDSCHIRM
        // ========================================
        "my_bookings.service" to "Dienstleistung",
        "my_bookings.date_time" to "Datum & Uhrzeit",
        "my_bookings.hairstylist" to "Friseur",
        "my_bookings.amount" to "Betrag",
        "my_bookings.review_left" to "Bewertung abgegeben",
        "my_bookings.leave_review" to "Eine Bewertung abgeben",
        "my_bookings.cancel" to "Abbrechen",
        "my_bookings.cancel_confirm" to "Ja, abbrechen",
        "my_bookings.cancel_keep" to "Nein, behalten",
        
        // ========================================
        // BUCHUNGSBILDSCHIRM (ASSISTENT)
        // ========================================
        "booking.back_to_home" to "Zurück zum Start",
        "booking.duration" to "Dauer",
        "booking.category" to "Kategorie",
        "booking.choose_expert" to "Wählen Sie Ihren Experten",
        "booking.choose_expert_subtitle" to "Wählen Sie einen Friseur oder lassen Sie den Salon wählen",
        "booking.select_date" to "Wählen Sie ein Datum",
        "booking.select_date_subtitle" to "Wählen Sie Ihren Termintag",
        "booking.choose_time_slot" to "Wählen Sie einen Zeitslot",
        "booking.choose_time_slot_subtitle" to "Wählen Sie die Zeit, die Ihnen passt",
        "booking.notes_or_special_requests" to "Notizen oder besondere Wünsche",
        "booking.notes_or_special_requests_subtitle" to "Optional - Teilen Sie uns Ihre Präferenzen mit",
        "booking.notes_info" to "Diese Informationen werden an Ihren Friseur weitergegeben",
        "booking.expert_selected" to "Experte ausgewählt",
        "booking.continue" to "Weiter",
        "booking.time_slot_selected" to "Zeitslot ausgewählt",
        "booking.pay_now" to "Jetzt bezahlen",
        "booking.view_my_bookings" to "Meine Buchungen anzeigen",
        
        // ========================================
        // BUCHUNGSDETAILBILDSCHIRM
        // ========================================
        "booking_detail.cancel_keep" to "Nein, behalten",
        "booking_detail.hair_salon" to "Friseursalon",
        "booking_detail.click_for_details" to "Klicken Sie für Details",
        "booking_detail.service" to "Dienstleistung",
        "booking_detail.service_details" to "Details zu Ihrer Dienstleistung",
        "booking_detail.booking" to "Buchung",
        "booking_detail.booking_details" to "Datum, Uhrzeit und Friseur",
        "booking_detail.payment" to "Zahlung",
        "booking_detail.payment_details" to "Transaktionsdetails",
        "booking_detail.amount" to "BETRAG",
        "booking_detail.cancel_booking_title" to "Buchung stornieren?",
        "booking_detail.cancel_confirm_message" to "Sind Sie sicher, dass Sie diese Buchung stornieren möchten? Diese Aktion ist unwiderruflich.",
        "booking_detail.booking_status" to "Status Ihrer Buchung",
        
        // ========================================
        // SALONDETAILBILDSCHIRM
        // ========================================
        "salon_detail.services" to "Dienstleistungen",
        "salon_detail.view_posts" to "Beiträge anzeigen",
        "salon_detail.open_feed" to "Feed öffnen",
        "salon_detail.add_member" to "Ein Mitglied hinzufügen",
        "salon_detail.queue" to "Warteschlange",
        "salon_detail.position" to "Position: #{position}",
        "salon_detail.leave_queue" to "Warteschlange verlassen",
        "salon_detail.join_queue" to "Warteschlange beitreten",
        "salon_detail.login_to_join_queue" to "Melden Sie sich an, um der Warteschlange beizutreten",
        "salon_detail.cancel" to "Abbrechen",
        "salon_detail.subscribers" to "Abonnenten",
        "salon_detail.book" to "Buchen",
        "salon_detail.salon_verified" to "Verifizierter Salon",
        "salon_detail.clients" to "Kunden",
        "salon_detail.waiting" to "Warten",
        "salon_detail.your_position" to "Ihre Position",
        "salon_detail.position_in_queue" to "Nummer {position} in der Warteschlange",
        "salon_detail.my_team" to "Mein Team",
        "salon_detail.team" to "Team",
        "salon_detail.our_services" to "Unsere Dienstleistungen",
        "salon_detail.choose_from_services" to "Wählen Sie aus {count} Dienstleistung",
        "salon_detail.choose_from_services_plural" to "Wählen Sie aus {count} Dienstleistungen",
        "salon_detail.loading_services" to "Dienstleistungen werden geladen",
        "salon_detail.please_wait" to "Bitte warten...",
        "salon_detail.loading_error" to "Ladefehler",
        "salon_detail.no_services_available" to "Keine Dienstleistungen verfügbar",
        "salon_detail.no_services_message" to "Dieser Salon hat noch keine Dienstleistungen hinzugefügt.\nKommen Sie später wieder!",
        "salon_detail.ready_to_book" to "Bereit zu buchen?",
        "salon_detail.reviews_and_ratings" to "Bewertungen & Noten",
        
        // ========================================
        // SALON ERSTELLEN BILDSCHIRM
        // ========================================
        "create_salon.launch_your_salon" to "Starten Sie Ihren Salon",
        "create_salon.join_community" to "Werden Sie Teil unserer Gemeinschaft von Profis",
        "create_salon.salon_info" to "Saloninformationen",
        "create_salon.cover_photo" to "Titelbild",
        "create_salon.cover_photo_hint" to "Optional • JPG, PNG • Max. 10MB",
        "create_salon.add_photo" to "Ein Foto hinzufügen",
        "create_salon.click_to_browse" to "Klicken Sie, um Ihre Dateien zu durchsuchen",
        "create_salon.data_secured" to "Ihre Daten sind sicher und geschützt",
        
        // ========================================
        // BEITRAGSDETAILBILDSCHIRM
        // ========================================
        "post_detail.comments" to "{count} Kommentar",
        "post_detail.comments_plural" to "{count} Kommentare",
        
        // ========================================
        // BEITRAG ERSTELLEN BILDSCHIRM
        // ========================================
        "create_post.public" to "Öffentlich",
        "create_post.post_type" to "Beitragstyp",
        "create_post.visibility" to "Sichtbarkeit",
        "create_post.at_least_two_images" to "Mindestens 2 Bilder (Vorher + Nachher)",
        "create_post.click_to_select_photo" to "Klicken Sie, um ein Foto auszuwählen",
        "create_post.photo_format_hint" to "JPG, PNG (max. 10MB)",
        "create_post.post_will_be_visible" to "Ihr Beitrag wird für alle Benutzer sichtbar sein",
        
        // ========================================
        // FRISEURPROFILBILDSCHIRM
        // ========================================
        "coiffeur_profile.pinned_posts" to "Angeheftete Beiträge",
        "coiffeur_profile.recent_posts" to "Aktuelle Beiträge",
        "coiffeur_profile.unfollow" to "Nicht mehr folgen",
        "coiffeur_profile.follow" to "Folgen",
        "coiffeur_profile.badges_and_certifications" to "Abzeichen und Zertifikate",
        "coiffeur_profile.featured_portfolio" to "Ausgewähltes Portfolio",
        "coiffeur_profile.portfolios" to "Portfolios",
        
        // ========================================
        // SOZIALES SALONPROFILBILDSCHIRM
        // ========================================
        "salon_social_profile.recent_posts" to "Aktuelle Beiträge",
        "salon_social_profile.unfollow" to "Nicht mehr folgen",
        "salon_social_profile.follow" to "Folgen",
        "salon_social_profile.featured_posts" to "Ausgewählte Beiträge",
        "salon_social_profile.portfolios" to "Portfolios",
        "salon_social_profile.verified" to "Verifiziert",
        "salon_social_profile.services" to "Dienstleistungen",
        
        // ========================================
        // CLIENTEN-PROFIL-BILDSCHIRM
        // ========================================
        "client_profile.title" to "Kundenprofil",
        "client_profile.about" to "Über",
        "client_profile.posts" to "Beiträge",
        "client_profile.likes" to "Gefällt mir",
        "client_profile.followers" to "Follower",
        "client_profile.following" to "Folge ich",
        "client_profile.collections" to "Sammlungen",
        "client_profile.collections_count" to "Sammlungen",
        "client_profile.recent_posts" to "Aktuelle Beiträge",
        "client_profile.badges" to "Abzeichen",
        "client_profile.follow" to "Folgen",
        "client_profile.unfollow" to "Nicht mehr folgen",
        
        // ========================================
        // SALONBESITZER-PROFIL-BILDSCHIRM
        // ========================================
        "salon_owner_profile.title" to "Salonbesitzer-Profil",
        "salon_owner_profile.about" to "Über",
        "salon_owner_profile.posts" to "Beiträge",
        "salon_owner_profile.likes" to "Gefällt mir",
        "salon_owner_profile.followers" to "Follower",
        "salon_owner_profile.salons" to "Salons",
        "salon_owner_profile.collections" to "Sammlungen",
        "salon_owner_profile.collections_count" to "Sammlungen",
        "salon_owner_profile.recent_posts" to "Aktuelle Beiträge",
        "salon_owner_profile.badges" to "Abzeichen",
        "salon_owner_profile.follow" to "Folgen",
        "salon_owner_profile.unfollow" to "Nicht mehr folgen",
        
        // ========================================
        // KOMMENTARBILDSCHIRM
        // ========================================
        "comments.title" to "Kommentare",
        "comments.add_comment" to "Kommentar hinzufügen...",
        "comments.load_more" to "Mehr Kommentare laden",
        
        // ========================================
        // SAMMLUNGSBILDSCHIRM
        // ========================================
        "collections.title" to "Sammlungen",
        "collections.post" to "Beitrag",
        "collections.posts" to "Beiträge",
        "collections.edit" to "Bearbeiten",
        "collections.delete" to "Löschen",
        "collections.new_collection" to "Neue Sammlung",
        "collections.name" to "Name *",
        "collections.description" to "Beschreibung (optional)",
        "collections.category" to "Kategorie",
        "collections.public_collection" to "Öffentliche Sammlung",
        "collections.save_to_collection" to "In Sammlung speichern",
        "collections.no_collections" to "Keine Sammlungen. Erstelle zuerst eine!",
        
        // ========================================
        // SAMMLUNGSDETAILBILDSCHIRM
        // ========================================
        "collection_detail.title" to "Sammlung",
        "collection_detail.load_more" to "Mehr laden",
        "collection_detail.edit" to "Bearbeiten",
        "collection_detail.delete" to "Löschen",
        "collection_detail.delete_collection" to "Sammlung löschen",
        "collection_detail.delete_collection_message" to "Sind Sie sicher, dass Sie diese Sammlung löschen möchten? Alle Posts werden entfernt.",
        "collection_detail.remove_from_collection" to "Aus Sammlung entfernen",
        
        // ========================================
        // PORTFOLIO ERSTELLEN BILDSCHIRM
        // ========================================
        "create_portfolio.title" to "Ein Portfolio erstellen",
        "create_portfolio.no_salon_found" to "Kein Salon gefunden. Bitte erstellen Sie zuerst einen Salon.",
        "create_portfolio.salon" to "Salon",
        "create_portfolio.cover_image" to "Titelbild",
        "create_portfolio.add_cover_image" to "Ein Titelbild hinzufügen",
        "create_portfolio.public_portfolio" to "Öffentliches Portfolio",
        "create_portfolio.name" to "Portfolioname *",
        "create_portfolio.name_placeholder" to "Z.B.: Meine Färbungen 2024",
        "create_portfolio.description" to "Beschreibung",
        
        // ========================================
        // PORTFOLIODETAILBILDSCHIRM
        // ========================================
        "portfolio_detail.load_more" to "Mehr laden",
        
        // ========================================
        // PORTFOLIOLISTENBILDSCHIRM
        // ========================================
        "portfolios_list.create_portfolio" to "Ein Portfolio erstellen",
        
        // ========================================
        // DIENSTLEISTUNG ERSTELLEN BILDSCHIRM
        // ========================================
        "create_service.premium_service" to "Premium-Dienstleistung",
        "create_service.service_info" to "Dienstleistungsinformationen",
        "create_service.category" to "Kategorie *",
        "create_service.price_adjustment_info" to "Preise können jederzeit von Ihrem professionellen Bereich aus angepasst werden.",
        "create_service.name" to "Servicename *",
        "create_service.name_placeholder" to "Z.B.: Herrenschnitt, Vollständige Färbung...",
        "create_service.description" to "Beschreibung",
        "create_service.duration" to "Dauer (Min) *",
        "create_service.price" to "Preis (€) *",
        
        // ========================================
        // SALONBEITRÄGE BILDSCHIRM
        // ========================================
        "salon_posts.title" to "Beiträge von {salonName}",
        "salon_posts.all" to "Alle",
        "salon_posts.all_services" to "Alle Dienste",
        "salon_posts.popular" to "Beliebt",
        
        // ========================================
        // ARCHIVBILDSCHIRM
        // ========================================
        "archives.title" to "Meine Archive",
        
        // ========================================
        // FAVORITENBILDSCHIRM
        // ========================================
        "favorites.title" to "Meine Favoriten",
        "favorites.offline_mode" to "Offline-Modus - Gecachte Daten",
        
        // ========================================
        // TRENDINGSBILDSCHIRM
        // ========================================
        "trending.title" to "Trends",
        "trending.posts" to "Beiträge",
        "trending.hashtags" to "Hashtags",
        "trending.salons" to "Salons",
        
        // ========================================
        // MELDUNGSBILDSCHIRM
        // ========================================
        "report.info_message" to "Ihr Bericht wird von unserem Moderations-Team überprüft. Vielen Dank, dass Sie uns helfen, eine respektvolle Gemeinschaft aufrechtzuerhalten.",
        "report.reason_title" to "Grund der Meldung *",
        "report.additional_info" to "Zusätzliche Informationen (optional)",
        "report.cancel" to "Abbrechen",
        "report.submit" to "Melden",
        "report.reported_content" to "Gemeldeter Inhalt",
        "report.post_author" to "Beitragsautor",
        
        // ========================================
        // PERSONAL ERSTELLEN BILDSCHIRM
        // ========================================
        "create_staff.title" to "Ein Mitglied hinzufügen",
        "create_staff.new_collaborator" to "Neuer Mitarbeiter",
        "create_staff.personal_info" to "Persönliche Informationen",
        "create_staff.specialties" to "Spezialitäten",
        "create_staff.specialties_hint" to "Wählen Sie die Dienstleistungskategorien aus, die dieses Mitglied durchführen kann.",
        "create_staff.email_info" to "Der Mitarbeiter erhält eine E-Mail mit seinen Anmeldedaten.",
        "create_staff.first_name" to "Vorname *",
        "create_staff.first_name_placeholder" to "Z.B.: Hans",
        "create_staff.last_name" to "Nachname *",
        "create_staff.last_name_placeholder" to "Z.B.: Müller",
        "create_staff.email" to "E-Mail *",
        
        // ========================================
        // WARTSCHLANGENVERWALTUNGSBILDSCHIRM
        // ========================================
        "queue_management.title" to "{salonName} - Warteschlangenverwaltung",
        "queue_management.ticket" to "Ticket #{position}",
        "queue_management.arrived" to "Angekommen",
        "queue_management.estimated_wait" to "Geschätzte Wartezeit",
        "queue_management.call_next" to "Nächsten aufrufen",
        "queue_management.remove" to "Entfernen",
        
        // ========================================
        // E-MAIL ÄNDERN
        // ========================================
        "change_email.title" to "E-Mail ändern",
        "change_email.current_email" to "Aktuelle E-Mail",
        "change_email.new_email_section" to "Neue E-Mail",
        "change_email.new_email" to "Neue E-Mail",
        "change_email.confirm_with_password" to "Mit Passwort bestätigen",
        "change_email.save" to "Speichern",
        "change_email.saving" to "Speichern...",
        "change_email.all_fields_required" to "Bitte füllen Sie alle Felder aus",
        "change_email.invalid_email" to "Ungültige E-Mail-Adresse",
        "change_email.error" to "Fehler beim Ändern der E-Mail",
        "change_email.info" to "Ihre neue E-Mail wird für die Anmeldung verwendet.",
        
        // ========================================
        // TELEFON ÄNDERN
        // ========================================
        "change_phone.title" to "Telefon ändern",
        "change_phone.current_phone" to "Aktuelle Telefonnummer",
        "change_phone.not_defined" to "Nicht festgelegt",
        "change_phone.new_phone_section" to "Neue Nummer",
        "change_phone.new_phone" to "Neue Telefonnummer",
        "change_phone.phone_placeholder" to "+49 170 1234567",
        "change_phone.leave_blank_to_remove" to "Leer lassen zum Entfernen",
        "change_phone.confirm_with_password" to "Mit Passwort bestätigen",
        "change_phone.save" to "Speichern",
        "change_phone.saving" to "Speichern...",
        "change_phone.password_required" to "Passwort ist erforderlich",
        "change_phone.error" to "Fehler beim Ändern der Telefonnummer",
        
        // ========================================
        // SICHERHEITSBILDSCHIRM
        // ========================================
        "security.title" to "Sicherheit",
        "security.change_password" to "Passwort ändern",
        "security.current_password" to "Aktuelles Passwort",
        "security.new_password" to "Neues Passwort",
        "security.confirm_password" to "Passwort bestätigen",
        "security.password_requirements" to "Mindestens 8 Zeichen",
        "security.change_password_button" to "Passwort ändern",
        "security.changing" to "Wird geändert...",
        "security.all_fields_required" to "Alle Felder sind erforderlich",
        "security.passwords_do_not_match" to "Passwörter stimmen nicht überein",
        "security.password_too_short" to "Das Passwort muss mindestens 8 Zeichen haben",
        "security.change_password_error" to "Fehler beim Ändern des Passworts",
        "security.password_changed" to "Passwort erfolgreich geändert",
        "security.active_sessions" to "Aktive Sitzungen",
        "security.no_active_sessions" to "Keine aktiven Sitzungen",
        "security.current_session" to "Diese Sitzung",
        "security.other_session" to "Anderes Gerät",
        "security.current_badge" to "Aktuell",
        "security.created_at" to "Erstellt am",
        "security.expires_at" to "Läuft ab am",
        "security.revoke" to "Widerrufen",
        "security.revoke_all" to "Alle widerrufen",
        "security.revoke_session_title" to "Sitzung widerrufen",
        "security.revoke_session_message" to "Möchten Sie dieses Gerät wirklich abmelden?",
        "security.revoke_all_sessions" to "Alle Geräte abmelden",
        "security.revoke_all_sessions_title" to "Alle Geräte abmelden",
        "security.revoke_all_sessions_message" to "Möchten Sie wirklich alle anderen Geräte abmelden? Sie müssen sich auf jedem Gerät erneut anmelden.",
        "security.session_revoked" to "Sitzung erfolgreich widerrufen",
        "security.all_sessions_revoked" to "Alle anderen Sitzungen wurden widerrufen",
        "security.revoke_session_error" to "Fehler beim Widerrufen der Sitzung",
        "security.revoke_all_sessions_error" to "Fehler beim Widerrufen der Sitzungen",
        "security.security_tip" to "Tipp: Ändern Sie Ihr Passwort regelmäßig und melden Sie Geräte ab, die Sie nicht erkennen.",
        
        // Passwortstärke-Indikator
        "security.password_strength" to "Passwortstärke",
        "security.password_weak" to "Schwach",
        "security.password_fair" to "Mittel",
        "security.password_good" to "Gut",
        "security.password_strong" to "Stark",
        
        // Sicherheitsstatus
        "security.security_status" to "Sicherheitsstatus",
        "security.account_protected" to "Ihr Konto ist geschützt",
        "security.active_devices" to "Aktive Geräte",
        "security.password_set" to "Passwort festgelegt",
        
        // Echtzeit-Updates
        "security.real_time_updates" to "Echtzeit-Aktualisierung",
        
        // Verbesserte Sicherheitstipps
        "security.security_tips_title" to "Sicherheitstipps",
        "security.tip_1" to "Verwenden Sie ein einzigartiges Passwort mit mindestens 12 Zeichen, Groß- und Kleinbuchstaben, Zahlen und Symbolen.",
        "security.tip_2" to "Teilen Sie Ihr Passwort niemals und aktivieren Sie die Zwei-Faktor-Authentifizierung, wenn verfügbar.",
        "security.tip_3" to "Überprüfen Sie regelmäßig Ihre aktiven Sitzungen und melden Sie verdächtige Geräte ab.",
        
        // ========================================
        // KONTO LÖSCHEN
        // ========================================
        "delete_account.title" to "Konto löschen",
        "delete_account.warning" to "Diese Aktion ist unwiderruflich. Alle Ihre Daten werden dauerhaft gelöscht.",
        "delete_account.confirm_title" to "Konto löschen?",
        "delete_account.confirm_message" to "Diese Aktion löscht dauerhaft Ihr Konto und alle Ihre Daten (Buchungen, Bewertungen, Fotos usw.). Diese Aktion ist unwiderruflich.",
        "delete_account.password_label" to "Geben Sie Ihr Passwort zur Bestätigung ein",
        "delete_account.confirm_checkbox" to "Ich verstehe, dass diese Aktion unwiderruflich ist",
        "delete_account.delete_button" to "Mein Konto löschen",
        "delete_account.deleting" to "Wird gelöscht...",
        "delete_account.success" to "Ihr Konto wurde erfolgreich gelöscht",
        "delete_account.error" to "Fehler beim Löschen des Kontos",
        
        // ========================================
        // ZAHLUNGSMETHODEN
        // ========================================
        "payment_methods.title" to "Zahlungsmethoden",
        "payment_methods.no_cards" to "Keine Karten gespeichert",
        "payment_methods.description" to "Ihre Zahlungskarten werden bei Ihrem nächsten Kauf sicher gespeichert.",
        "payment_methods.stripe_info" to "Ihre Zahlungsinformationen sind durch Stripe geschützt. Wir speichern niemals Ihre Kartennummern.",
        "payment_methods.add_card" to "Karte hinzufügen",
        
        // ========================================
        // BLOCKIERTE BENUTZER
        // ========================================
        "blocked_users.title" to "Blockierte Benutzer",
        "blocked_users.no_blocked_users" to "Keine blockierten Benutzer",
        "blocked_users.description" to "Benutzer, die Sie blockieren, können Ihr Profil nicht sehen und Sie nicht kontaktieren.",
        "blocked_users.unblock" to "Entsperren",
        
        // ========================================
        // HILFEZENTRUM
        // ========================================
        "help_center.title" to "Hilfezentrum",
        "help_center.faq_title" to "Häufig gestellte Fragen",
        "help_center.need_more_help" to "Keine Antwort gefunden?",
        "help_center.contact_support" to "Support kontaktieren",
        "help_center.faq1_question" to "Wie storniere ich eine Buchung?",
        "help_center.faq1_answer" to "Gehen Sie zu 'Meine Buchungen', wählen Sie die Buchung aus und tippen Sie auf 'Stornieren'. Stornierung ist bis 24 Stunden vor dem Termin kostenlos.",
        "help_center.faq2_question" to "Wie bearbeite ich mein Profil?",
        "help_center.faq2_answer" to "Gehen Sie zu Einstellungen > Mein Konto > Profil bearbeiten, um Ihre persönlichen Daten zu aktualisieren.",
        "help_center.faq3_question" to "Wie kontaktiere ich einen Salon?",
        "help_center.faq3_answer" to "Auf der Salon-Seite finden Sie die Kontaktinformationen: Telefon, E-Mail und Adresse.",
        "help_center.faq4_question" to "Sind Zahlungen sicher?",
        "help_center.faq4_answer" to "Ja, alle Zahlungen werden über Stripe, einem weltweit führenden Anbieter für Online-Zahlungen, verarbeitet. Ihre Bankdaten werden niemals auf unseren Servern gespeichert.",
        
        // ========================================
        // SUPPORT KONTAKTIEREN
        // ========================================
        "contact.title" to "Support kontaktieren",
        "contact.response_time" to "Wir antworten normalerweise innerhalb von 24-48 Stunden.",
        "contact.send_message" to "Nachricht senden",
        "contact.subject" to "Betreff",
        "contact.message" to "Ihre Nachricht",
        "contact.send" to "Senden",
        "contact.sending" to "Wird gesendet...",
        
        // ========================================
        // NUTZUNGSBEDINGUNGEN
        // ========================================
        "terms.title" to "Nutzungsbedingungen",
        "terms.last_update" to "Letzte Aktualisierung: Januar 2025",
        "terms.section1_title" to "1. Akzeptanz der Bedingungen",
        "terms.section1_content" to "Durch die Nutzung der Frollot-App stimmen Sie diesen Nutzungsbedingungen zu. Wenn Sie nicht zustimmen, nutzen Sie die App bitte nicht.",
        "terms.section2_title" to "2. Servicebeschreibung",
        "terms.section2_content" to "Frollot ist eine Plattform, die Friseursalons mit ihren Kunden verbindet. Sie ermöglicht Online-Buchung, Zahlung und Terminverfolgung.",
        "terms.section3_title" to "3. Benutzerkonto",
        "terms.section3_content" to "Sie sind für die Vertraulichkeit Ihrer Anmeldedaten und alle Aktivitäten unter Ihrem Konto verantwortlich.",
        "terms.section4_title" to "4. Zahlungen und Stornierungen",
        "terms.section4_content" to "Zahlungen werden sicher über Stripe verarbeitet. Die Stornierungsbedingungen variieren je nach Salon.",
        
        // ========================================
        // DATENSCHUTZRICHTLINIE
        // ========================================
        "privacy.title" to "Datenschutzrichtlinie",
        "privacy.last_update" to "Letzte Aktualisierung: Januar 2025",
        "privacy.section1_title" to "1. Erfasste Daten",
        "privacy.section1_content" to "Wir erfassen Daten, die Sie uns mitteilen (Name, E-Mail, Telefon) sowie App-Nutzungsdaten, um unsere Dienste zu verbessern.",
        "privacy.section2_title" to "2. Verwendung der Daten",
        "privacy.section2_content" to "Ihre Daten werden verwendet, um Ihre Buchungen zu verwalten, Ihnen wichtige Benachrichtigungen zu senden und Ihre Benutzererfahrung zu verbessern.",
        "privacy.section3_title" to "3. Datenweitergabe",
        "privacy.section3_content" to "Wir teilen Ihre Buchungsinformationen mit den entsprechenden Salons. Wir verkaufen Ihre persönlichen Daten niemals an Dritte.",
        "privacy.section4_title" to "4. Ihre Rechte",
        "privacy.section4_content" to "Sie haben das Recht, jederzeit über Ihre Kontoeinstellungen auf Ihre persönlichen Daten zuzugreifen, sie zu ändern oder zu löschen.",
        
        // ========================================
        // ZAHLUNGSBILDSCHIRM
        // ========================================
        "payment.title" to "Zahlung",
        "payment.cancel" to "Abbrechen",
        "payment.service" to "Service:",
        "payment.salon" to "Salon:",
        "payment.date" to "Datum:",
        
        // ========================================
        // VERIFIZIERUNGSANFRAGEBILDSCHIRM
        // ========================================
        "request_verification.title" to "Verifizierung anfordern",
        "request_verification.header_title" to "Verifizierungsanfrage",
        "request_verification.description" to "Wählen Sie den Typ der Verifizierung aus, die Sie erhalten möchten. Unser Team wird Ihre Anfrage in Kürze überprüfen.",
        "request_verification.verification_type" to "Verifizierungstyp *",
        "request_verification.additional_info" to "Zusätzliche Informationen (optional)",
        "request_verification.additional_info_placeholder" to "Beschreiben Sie Ihre Situation, stellen Sie Dokumente bereit (SIRET, Diplome, etc.)...",
        
        // ========================================
        // BEWERTUNG ERSTELLEN BILDSCHIRM
        // ========================================
        "create_review.title" to "Eine Bewertung abgeben",
        "create_review.your_booking" to "Ihre Buchung",
        "create_review.salon" to "Salon: {salonName}",
        "create_review.date" to "Datum: {date}",
        "create_review.rating_question" to "Wie bewerten Sie Ihre Erfahrung?",
        "create_review.title_label" to "Bewertungstitel (optional)",
        "create_review.title_placeholder" to "Z.B.: Tolle Erfahrung!",
        "create_review.comment_label" to "Ihr Kommentar (optional)",
        "create_review.comment_placeholder" to "Teilen Sie Ihre Erfahrung...",
        
        // ========================================
        // BUCHUNGSVERWALTUNGSBILDSCHIRM DES BESITZERS
        // ========================================
        "owner_bookings_management.title" to "Terminverwaltung",
        "owner_bookings_management.confirm" to "Bestätigen",
        "owner_bookings_management.start" to "Starten",
        "owner_bookings_management.absent" to "Abwesend",
        "owner_bookings_management.finish" to "Beenden",
        
        // ========================================
        // SUCHBILDSCHIRM
        // ========================================
        "search.title" to "Suchen",
        "search.advanced_filters" to "Erweiterte Filter",
        "search.post_type" to "Beitragstyp",
        "search.search_placeholder" to "Nach Beiträgen, Salons, Benutzern suchen...",
        "search.no_posts_found" to "Keine Beiträge gefunden",
        "search.no_salons_found" to "Keine Salons gefunden",
        "search.no_users_found" to "Keine Benutzer gefunden",
        "search.no_hashtags_found" to "Keine Hashtags gefunden",
        "search.no_results_found" to "Keine Ergebnisse gefunden",
        "search.all" to "Alle",
        "search.load_more" to "Mehr laden",
        "search.posts" to "Beiträge ({count})",
        "search.salons" to "Salons ({count})",
        "search.users" to "Benutzer ({count})",
        "search.hashtags" to "Hashtags ({count})",
        
        // ========================================
        // ULTRA PREMIUM POST CARD KOMPONENTE
        // ========================================
        "ultra_premium_post_card.oops" to "Ups!",
        "ultra_premium_post_card.retry" to "Wiederholen",
        "ultra_premium_post_card.no_posts_yet" to "Noch keine Beiträge",
        "ultra_premium_post_card.be_first_to_share" to "Seien Sie der Erste, der etwas\nUnglaubliches teilt!",
        "ultra_premium_post_card.view_comments" to "Kommentar anzeigen",
        "ultra_premium_post_card.view_comments_plural" to "{count} Kommentare anzeigen",
        "ultra_premium_post_card.add_comment" to "Einen Kommentar hinzufügen...",
        "ultra_premium_post_card.pinned" to "Angeheftet",
        
        // ========================================
        // GEMEINSAME SCHALTFLÄCHEN UND AKTIONEN
        // ========================================
        "common.cancel" to "Abbrechen",
        "common.save" to "Speichern",
        "common.confirm" to "Bestätigen",
        "common.retry" to "Wiederholen",
        "common.delete" to "Löschen",
        "common.edit" to "Bearbeiten",
        "common.remove" to "Entfernen",
        "common.add" to "Hinzufügen",
        "common.create" to "Erstellen",
        "common.load_more" to "Mehr laden",
        "common.all" to "Alle",
        "common.start" to "Starten",
        "common.finish" to "Beenden",
        "common.absent" to "Abwesend",
        "common.popular" to "Beliebt",
        "common.public" to "Öffentlich",
        "common.service" to "Dienstleistung:",
        "common.salon" to "Salon:",
        "common.date" to "Datum:",
        "common.verified" to "Verifiziert",
        
        // ========================================
        // UI-KOMPONENTEN
        // ========================================
        // RatingBar
        "components.rating_bar.star_filled" to "Stern {number} gefüllt",
        "components.rating_bar.star_empty" to "Stern {number} leer",
        "components.rating_bar.reviews_count" to "({count} Bewertungen)",
        
        // PasswordTextField
        "components.password_text_field.label" to "Passwort",
        "components.password_text_field.placeholder" to "Geben Sie Ihr Passwort ein",
        "components.password_text_field.show_password" to "Passwort anzeigen",
        "components.password_text_field.hide_password" to "Passwort ausblenden",
        
        // ReportDialog
        "components.report_dialog.title" to "Dieses {entity} melden",
        "components.report_dialog.info_message" to "Ihr Bericht wird von unserem Moderations-Team überprüft. Vielen Dank, dass Sie uns helfen, eine respektvolle Gemeinschaft zu erhalten.",
        "components.report_dialog.reason_label" to "Grund für die Meldung *",
        "components.report_dialog.additional_info_label" to "Zusätzliche Informationen (optional)",
        "components.report_dialog.additional_info_placeholder" to "Beschreiben Sie kurz das Problem...",
        "components.report_dialog.error_select_reason" to "Bitte wählen Sie einen Grund aus",
        "components.report_dialog.error_reporting" to "Fehler beim Melden: {error}",
        "components.report_dialog.error_unknown" to "Unbekannter Fehler",
        
        // UserAvatar
        "components.user_avatar.content_description" to "Avatar von {name}",
        
        // QueueStatusCard
        "components.queue_status_card.connection_lost" to "Verbindung verloren",
        "components.queue_status_card.data_stale" to "Veraltete Daten",
        "components.queue_status_card.last_update" to "Letzte Aktualisierung: vor {minutes} Min",
        "components.queue_status_card.your_progress" to "Ihr Fortschritt",
        "components.queue_status_card.current_position" to "Aktuelle Position",
        "components.queue_status_card.estimated_time" to "Geschätzte Zeit",
        "components.queue_status_card.just_now" to "Gerade eben",
        "components.queue_status_card.minutes_ago" to "Vor {minutes} Min",
        "components.queue_status_card.leave_queue" to "Warteschlange verlassen",
        "components.queue_status_card.reconnecting" to "Automatische Wiederverbindung läuft...",
        "components.queue_status_card.keep_app_open" to "Halten Sie die App offen, um vom Salon benachrichtigt zu werden.",
        "components.queue_status_card.status_offline" to "❌ Offline",
        "components.queue_status_card.status_pending" to "⏸️ Aktualisierung ausstehend...",
        "components.queue_status_card.status_auto_refresh" to "✅ Automatische Aktualisierung (30s)",
        
        // FullScreenImageViewer
        "components.full_screen_image_viewer.image_content_description" to "Bild {number}",
        "components.full_screen_image_viewer.close" to "Schließen",
        
        // AppDrawer
        "components.app_drawer.marketplace" to "Marktplatz",
        "components.app_drawer.social" to "Sozial",
        "components.app_drawer.appointments" to "Termine",
        "components.app_drawer.account" to "Konto",
        "components.app_drawer.profile" to "Profil",
        "components.app_drawer.notifications" to "Benachrichtigungen",
        "components.app_drawer.favorites" to "Favoriten",
        "components.app_drawer.archives" to "Archive",
        "components.app_drawer.collections" to "Sammlungen",
        "components.app_drawer.management" to "Verwaltung",
        "components.app_drawer.my_salons" to "Meine Salons",
        "components.app_drawer.new_salon" to "Neuer Salon",
        "components.app_drawer.create_post" to "Beitrag erstellen",
        "components.app_drawer.bookings_management" to "Terminverwaltung",
        "components.app_drawer.stats" to "Statistiken",
        "components.app_drawer.activity" to "Aktivität",
        "components.app_drawer.my_portfolios" to "Meine Portfolios",
        "components.app_drawer.new_portfolio" to "Neues Portfolio",
        "components.app_drawer.services" to "Dienstleistungen",
        "components.app_drawer.agenda" to "Agenda",
        "components.app_drawer.admin" to "Admin",
        "components.app_drawer.dashboard" to "Dashboard",
        "components.app_drawer.users" to "Benutzer",
        "components.app_drawer.settings" to "Einstellungen",
        "components.app_drawer.help" to "Hilfe",
        "components.app_drawer.guest" to "Gast",
        "components.app_drawer.client" to "Kunde",
        "components.app_drawer.owner" to "Eigentümer",
        "components.app_drawer.hairstylist" to "Friseur",
        "components.app_drawer.admin_user" to "Admin",
        
        // ========================================
        // ZAHLUNGSBILDSCHIRM
        // ========================================
        "payment.title" to "Zahlung",
        "payment.cancel" to "Abbrechen",
        "payment.service" to "Dienstleistung",
        "payment.salon" to "Salon",
        "payment.date" to "Datum",
        "payment.card_input" to "Karte",
        "payment.confirmation" to "Bestätigung",
        "payment.processing" to "Verarbeitung...",
        "payment.success" to "Zahlung erfolgreich",
        "payment.error" to "Fehler",
        "payment.continue" to "Weiter",
        "payment.retry" to "Erneut versuchen",
        "payment.pay_amount" to "{amount}€ bezahlen",
        "payment.verify_order" to "Bestellung überprüfen",
        "payment.booking_details" to "Buchungsdetails",
        "payment.card" to "Karte",
        "payment.total_to_pay" to "Gesamtbetrag",
        "payment.modify_card" to "Karte ändern",
        "payment.processing_message" to "Bitte warten Sie, während wir Ihre Zahlung sicher verarbeiten.",
        "payment.success_message" to "Ihre Buchung ist bestätigt. Sie erhalten eine Bestätigungs-E-Mail.",
        "payment.error_message" to "Bei der Zahlung ist ein Fehler aufgetreten. Bitte versuchen Sie es erneut.",
        "payment.view_booking" to "Meine Buchung ansehen",
        "payment.card_number" to "Kartennummer",
        "payment.card_holder" to "Name auf der Karte",
        "payment.expiry" to "Ablaufdatum",
        "payment.cvv" to "CVV",
        "payment.ssl" to "SSL 256-bit",
        "payment.pci_dss" to "PCI-DSS",
        "payment.3d_secure" to "3D Secure",
        "payment.history" to "Zahlungsverlauf",
        "payment.no_payments" to "Sie haben noch keine Zahlungen getätigt.",
        "payment.summary" to "Zusammenfassung",
        "payment.total_spent" to "Gesamtausgaben",
        "payment.transactions" to "Transaktionen",
        "payment.success_rate" to "Erfolgsrate",
        "payment.all_payments" to "Alle",
        "payment.succeeded" to "Erfolgreich",
        "payment.failed" to "Fehlgeschlagen",
        "payment.refunded" to "Erstattet",
        "payment.transaction_id" to "Transaktions-ID",
        "payment.stripe_reference" to "Stripe-Referenz",
        "payment.payment_method" to "Zahlungsmethode",
        "payment.currency" to "Währung",
        "payment.refunded_amount" to "Erstatteter Betrag",
        
        // Stripe Checkout
        "payment.redirect" to "Weiterleitung",
        "payment.secure_payment" to "Sichere Zahlung",
        "payment.secure_payment_description" to "Sie werden zur sicheren Stripe-Zahlungsseite weitergeleitet.",
        "payment.order_summary" to "Bestellübersicht",
        "payment.total" to "Gesamt",
        "payment.loading" to "Lädt...",
        "payment.proceed_to_payment" to "Zur Zahlung",
        "payment.secure_ssl" to "SSL 256-bit",
        "payment.stripe_secure" to "Stripe",
        "payment.pci_compliant" to "PCI-DSS",
        "payment.redirect_to_stripe" to "Zahlungsseite",
        "payment.redirect_description" to "Schließen Sie Ihre Zahlung auf der Stripe-Seite ab.\nKehren Sie nach der Zahlung hierher zurück.",
        "payment.open_payment_page" to "Zahlungsseite öffnen",
        "payment.check_payment_status" to "Zahlungsstatus prüfen",
        "payment.processing_payment" to "Verarbeitung...",
        "payment.processing_description" to "Bitte warten Sie, während wir Ihre Zahlung überprüfen.",
        "payment.payment_successful" to "Zahlung erfolgreich! 🎉",
        "payment.payment_success_description" to "Ihre Buchung ist bestätigt.\nSie erhalten eine Bestätigungs-E-Mail.",
        "payment.payment_failed" to "Zahlung fehlgeschlagen",
        "payment.payment_failed_description" to "Bei der Zahlung ist ein Fehler aufgetreten.\nBitte versuchen Sie es erneut.",
        "payment.step_summary" to "Übersicht",
        "payment.step_payment" to "Zahlung",
        "payment.step_confirmation" to "Bestätigung",
        
        // ExternalShareDialog
        "components.external_share_dialog.title" to "Teilen an",
        "components.external_share_dialog.share_via_app" to "Über eine App teilen",
        "components.external_share_dialog.share_via_app_description" to "Instagram, WhatsApp, Nachrichten, etc.",
        "components.external_share_dialog.error_sharing" to "Fehler beim Teilen: {error}",
        "components.external_share_dialog.copy_link" to "Link kopieren",
        "components.external_share_dialog.copy_link_description" to "Link des Beitrags in die Zwischenablage kopieren",
        "components.external_share_dialog.error_copying" to "Fehler beim Kopieren: {error}",
        "components.external_share_dialog.not_available" to "Externes Teilen ist auf dieser Plattform nicht verfügbar",
        
        // SearchTextField
        "components.search_text_field.placeholder" to "Suchen...",
        "components.search_text_field.content_description" to "Suchen",
        
        // ========================================
        // ENUMS - LOKALISIERUNG
        // ========================================
        // BookingStatus
        "enums.booking_status.pending" to "Ausstehend",
        "enums.booking_status.confirmed" to "Bestätigt",
        "enums.booking_status.in_progress" to "In Bearbeitung",
        "enums.booking_status.completed" to "Abgeschlossen",
        "enums.booking_status.cancelled" to "Storniert",
        "enums.booking_status.no_show" to "Nicht erschienen",
        
        // PaymentStatus
        "enums.payment_status.pending" to "Ausstehend",
        "enums.payment_status.processing" to "Wird verarbeitet",
        "enums.payment_status.succeeded" to "Erfolgreich",
        "enums.payment_status.failed" to "Fehlgeschlagen",
        "enums.payment_status.canceled" to "Storniert",
        "enums.payment_status.partially_refunded" to "Teilweise erstattet",
        "enums.payment_status.unpaid" to "Nicht bezahlt",
        "enums.payment_status.paid" to "Bezahlt",
        "enums.payment_status.refunded" to "Erstattet",
        
        // PostType
        "enums.post_type.general" to "Allgemein",
        "enums.post_type.avant_apres" to "Vorher/Nachher",
        "enums.post_type.portfolio" to "Portfolio",
        "enums.post_type.tendance" to "Trend",
        "enums.post_type.conseil" to "Rat",
        "enums.post_type.realisation" to "Leistung",
        "enums.post_type.inspiration" to "Inspiration",
        "enums.post_type.general_description" to "Allgemeiner Beitrag",
        "enums.post_type.avant_apres_description" to "Vorher/Nachher-Transformation zeigen",
        "enums.post_type.portfolio_description" to "Zu Ihrem Portfolio hinzufügen",
        "enums.post_type.tendance_description" to "Haartrend teilen",
        "enums.post_type.conseil_description" to "Tipps und Ratschläge geben",
        "enums.post_type.realisation_description" to "Leistung zeigen",
        "enums.post_type.inspiration_description" to "Inspiration teilen",
        
        // PostVisibility
        "enums.post_visibility.public" to "Öffentlich",
        "enums.post_visibility.followers" to "Nur Follower",
        "enums.post_visibility.private" to "Privat",
        "enums.post_visibility.public_description" to "Für alle sichtbar",
        "enums.post_visibility.followers_description" to "Nur für Ihre Follower sichtbar",
        "enums.post_visibility.private_description" to "Nur für Sie sichtbar",
        
        // ServiceCategory
        "enums.service_category.coupe" to "Schnitt & Stil",
        "enums.service_category.coloration" to "Färbung",
        "enums.service_category.soin" to "Pflege",
        "enums.service_category.coiffage" to "Styling",
        "enums.service_category.barbe" to "Barbier",
        "enums.service_category.technique" to "Spezielle Techniken",
        "enums.service_category.autre" to "Andere Dienstleistungen",
        
        // ReactionType
        "enums.reaction_type.like" to "Gefällt mir",
        "enums.reaction_type.love" to "Ich liebe es",
        "enums.reaction_type.wow" to "Wow",
        "enums.reaction_type.inspirant" to "Inspirierend",
        "enums.reaction_type.magnifique" to "Großartig",
        "enums.reaction_type.bravo" to "Bravo",
        "enums.reaction_type.like_description" to "Klassisches Gefällt mir",
        "enums.reaction_type.love_description" to "Ich liebe diese Farbe!",
        "enums.reaction_type.wow_description" to "Unglaubliche Transformation!",
        "enums.reaction_type.inspirant_description" to "Ich will dasselbe!",
        "enums.reaction_type.magnifique_description" to "Qualitätsarbeit!",
        "enums.reaction_type.bravo_description" to "Gratulation an den Friseur!",
        
        // MediaType
        "enums.media_type.before" to "Vorher",
        "enums.media_type.after" to "Nachher",
        "enums.media_type.process" to "Prozess",
        "enums.media_type.detail" to "Detail",
        
        // ReportReason
        "enums.report_reason.inapproprie" to "Unangemessener Inhalt",
        "enums.report_reason.spam" to "Werbe-Spam",
        "enums.report_reason.faux" to "Falsches Vorher/Nachher",
        "enums.report_reason.copyright" to "Urheberrechtsverletzung",
        "enums.report_reason.autre" to "Andere",
        "enums.report_reason.inapproprie_description" to "Gewalttätiger, belästigender oder beleidigender Inhalt",
        "enums.report_reason.spam_description" to "Unaufgeforderte Werbung oder wiederholter Inhalt",
        "enums.report_reason.faux_description" to "Irreführende Transformation oder Ergebnis",
        "enums.report_reason.copyright_description" to "Unbefugte Nutzung geschützter Inhalte",
        "enums.report_reason.autre_description" to "Anderer Grund anzugeben",
        
        // ReportedEntityType
        "enums.reported_entity_type.post" to "Beitrag",
        "enums.reported_entity_type.comment" to "Kommentar",
        "enums.reported_entity_type.user" to "Benutzer",
        "enums.reported_entity_type.salon" to "Salon",
        
        // VerificationType
        "enums.verification_type.email" to "Verifizierte E-Mail",
        "enums.verification_type.phone" to "Verifiziertes Telefon",
        "enums.verification_type.business" to "Verifiziertes Unternehmen",
        "enums.verification_type.professional" to "Verifizierter Profi",
        "enums.verification_type.email_description" to "E-Mail durch Bestätigung verifiziert",
        "enums.verification_type.phone_description" to "Telefonnummer verifiziert",
        "enums.verification_type.business_description" to "Unternehmen verifiziert (SIRET, Dokumente)",
        "enums.verification_type.professional_description" to "Diplome und Zertifikate verifiziert",
        
        // BadgeCategory
        "enums.badge_category.certification" to "Zertifizierung",
        "enums.badge_category.competition" to "Wettbewerb",
        "enums.badge_category.formation" to "Ausbildung",
        "enums.badge_category.partenariat" to "Partnerschaft",
        
        // ReportStatus
        "enums.report_status.pending" to "Ausstehend",
        "enums.report_status.reviewed" to "In Überprüfung",
        "enums.report_status.resolved" to "Gelöst",
        "enums.report_status.dismissed" to "Abgelehnt",
        
        // ModerationAction
        "enums.moderation_action.hide" to "Verstecken",
        "enums.moderation_action.delete" to "Löschen",
        "enums.moderation_action.warn" to "Warnen",
        "enums.moderation_action.hide_description" to "Der Inhalt wird für alle Benutzer außer dem Autor und den Administratoren versteckt.",
        "enums.moderation_action.delete_description" to "Der Inhalt wird dauerhaft gelöscht und kann nicht wiederhergestellt werden.",
        "enums.moderation_action.warn_description" to "Eine Warnung wird an den Autor gesendet, ohne den Inhalt zu ändern.",

        // AppealStatus
        "enums.appeal_status.none" to "Kein Einspruch",
        "enums.appeal_status.pending" to "Ausstehend",
        "enums.appeal_status.approved" to "Genehmigt",
        "enums.appeal_status.rejected" to "Abgelehnt",

        // ModerationAppealStatus
        "enums.appeal_status.none" to "Kein Einspruch",
        "enums.appeal_status.pending" to "Ausstehend",
        "enums.appeal_status.approved" to "Genehmigt",
        "enums.appeal_status.rejected" to "Abgelehnt",
        
        // HairHashtagCategory
        "enums.hair_hashtag_category.technique" to "Technik",
        "enums.hair_hashtag_category.style" to "Stil",
        "enums.hair_hashtag_category.couleur" to "Farbe",
        "enums.hair_hashtag_category.longueur" to "Länge",
        "enums.hair_hashtag_category.texture" to "Textur",
        
        // FollowingType
        "enums.following_type.coiffeur" to "Friseur",
        "enums.following_type.salon" to "Salon",
    )
    
    return StringsBundle(strings)
}
