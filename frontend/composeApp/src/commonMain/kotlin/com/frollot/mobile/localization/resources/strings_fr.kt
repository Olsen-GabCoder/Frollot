package com.frollot.mobile.localization.resources

/**
 * Bundle de strings françaises.
 * 
 * Langue de référence obligatoire selon l'ADR-001 - DÉCISION 3.
 * Toutes les clés doivent être présentes dans ce fichier.
 * 
 * NOTE : Ce fichier sera progressivement rempli lors de la Phase 2
 * (migration des écrans). Pour l'instant, il est vide mais structurellement prêt.
 */
fun createFrenchStrings(): StringsBundle {
    val strings = mapOf<String, String>(
        // ========================================
        // ÉCRAN DE CONNEXION
        // ========================================
        "login.welcome_title" to "Bienvenue 👋",
        "login.welcome_subtitle" to "Connectez-vous et explorez\ndes possibilités infinies",
        "login.email_label" to "Adresse email",
        "login.email_placeholder" to "vous@exemple.com",
        "login.password_label" to "Mot de passe",
        "login.password_placeholder" to "••••••••",
        "login.forgot_password" to "Mot de passe oublié ?",
        "login.submit_button" to "Se connecter",
        "login.submit_button_loading" to "Connexion en cours...",
        "login.continue_with" to "OU CONTINUEZ AVEC",
        "login.google_button" to "Google",
        "login.facebook_button" to "Facebook",
        "login.new_here" to "Nouveau ici ?",
        "login.create_account" to "Créer un compte",
        "login.secure_connection" to "Connexion 100% sécurisée",
        
        // Messages d'erreur de connexion
        "login.errors.invalid_credentials" to "Email ou mot de passe incorrect",
        "login.errors.email_not_verified" to "Votre adresse email n'a pas été vérifiée. Veuillez vérifier votre email avant de vous connecter.",
        "login.errors.account_disabled" to "Compte désactivé. Veuillez contacter le support.",
        "login.errors.account_not_found" to "Compte introuvable",
        "login.errors.server_unavailable" to "Impossible de contacter le serveur",
        "login.errors.timeout" to "Délai d'attente dépassé. Vérifiez votre connexion internet.",
        "login.errors.generic_error" to "Erreur lors de la connexion",
        
        // ========================================
        // ÉCRAN D'INSCRIPTION
        // ========================================
        "register.welcome_title" to "Rejoignez-nous 🎉",
        "register.welcome_subtitle" to "Créez votre compte en quelques secondes",
        "register.first_name_label" to "Prénom",
        "register.first_name_placeholder" to "Sophie",
        "register.last_name_label" to "Nom",
        "register.last_name_placeholder" to "Martin",
        "register.email_label" to "Adresse email",
        "register.email_placeholder" to "vous@exemple.com",
        "register.account_type_label" to "Type de compte",
        "register.password_label" to "Mot de passe",
        "register.password_placeholder" to "Minimum 8 caractères",
        "register.confirm_password_label" to "Confirmer le mot de passe",
        "register.confirm_password_placeholder" to "Retapez votre mot de passe",
        "register.password_mismatch" to "Les mots de passe ne correspondent pas",
        "register.submit_button" to "Créer mon compte",
        "register.submit_button_loading" to "Création en cours...",
        "register.sign_up_with" to "OU INSCRIVEZ-VOUS AVEC",
        "register.google_button" to "Google",
        "register.facebook_button" to "Facebook",
        "register.already_registered" to "Déjà inscrit ?",
        "register.login_link" to "Se connecter",
        "register.data_protected" to "Vos données sont protégées",
        "register.success_message" to "Bienvenue {firstName} {lastName} ! Votre compte a été créé avec succès.",
        
        // Types de compte
        "register.user_types.client" to "👤 Client",
        "register.user_types.hairstylist" to "✂️ Coiffeur",
        "register.user_types.salon_owner" to "🏢 Propriétaire de salon",
        "register.user_types.admin" to "⚙️ Administrateur",
        
        // Messages d'erreur d'inscription
        "register.errors.email_already_used" to "Cet email est déjà utilisé",
        "register.errors.invalid_data" to "Données invalides, vérifiez vos informations",
        "register.errors.server_unavailable" to "Impossible de contacter le serveur",
        "register.errors.timeout" to "Délai d'attente dépassé. Vérifiez votre connexion internet.",
        "register.errors.generic_error" to "Erreur lors de l'inscription",
        
        // ========================================
        // ÉCRAN DE VÉRIFICATION EMAIL
        // ========================================
        "email_verification.title" to "Vérifiez votre email",
        "email_verification.subtitle" to "Un email de vérification a été envoyé à votre adresse",
        "email_verification.instructions" to "Veuillez entrer le code de vérification reçu par email, ou cliquez sur le lien dans l'email.",
        "email_verification.token_label" to "Code de vérification",
        "email_verification.token_placeholder" to "Entrez le code reçu par email",
        "email_verification.verify_button" to "Vérifier",
        "email_verification.verify_button_loading" to "Vérification en cours...",
        "email_verification.resend_button" to "Renvoyer l'email",
        "email_verification.resend_button_loading" to "Envoi en cours...",
        "email_verification.success_title" to "Email vérifié ! ✅",
        "email_verification.success_message" to "Votre adresse email a été vérifiée avec succès. Vous pouvez maintenant utiliser votre compte.",
        "email_verification.resend_success" to "Email renvoyé avec succès !",
        "email_verification.resend_instructions" to "Un nouvel email de vérification a été envoyé. Vérifiez votre boîte de réception.",
        
        // Messages d'erreur de vérification
        "email_verification.errors.invalid_token" to "Code de vérification invalide",
        "email_verification.errors.expired_token" to "Ce code a expiré. Veuillez demander un nouveau code.",
        "email_verification.errors.server_error" to "Erreur serveur. Veuillez réessayer.",
        "email_verification.errors.generic_error" to "Erreur lors de la vérification",
        
        // ========================================
        // ÉCRAN D'ACCUEIL
        // ========================================
        "home.title" to "Frollot",
        "home.filters_button" to "Filtres",
        "home.my_bookings_button" to "Mes réservations",
        "home.create_salon_button" to "Créer un salon",
        "home.loading_message" to "Chargement des salons",
        "home.error_message" to "Impossible de charger les salons",
        "home.empty_state_title" to "Aucun salon disponible",
        "home.empty_state_title_with_filters" to "Aucun salon ne correspond à vos critères",
        "home.empty_state_message" to "Soyez le premier à créer un salon !",
        "home.empty_state_message_with_filters" to "Essayez d'élargir votre recherche ou de retirer certains filtres.",
        "home.create_my_salon" to "Créer mon salon",
        "home.premium_salons" to "Salons Premium",
        "home.near_me" to "Près de moi",
        "home.city" to "Ville",
        "home.salons_found" to "{count} salon trouvé",
        "home.salons_found_plural" to "{count} salons trouvés",
        "home.filter_by_city" to "Filtrer par ville",
        "home.filter_by_city_description" to "Entrez une ville pour filtrer les salons.",
        "home.city_placeholder" to "Ex: Paris",
        "home.apply" to "Appliquer",
        "home.reset" to "Réinitialiser",
        "home.premium_badge" to "PREMIUM",
        "home.rating" to "Note",
        "home.open_hours" to "Ouvert • 9h-19h",
        "home.cut" to "Coupe",
        "home.coloring" to "Coloration",
        "home.book" to "Réserver",
        "home.cover_photo_description" to "Photo de couverture de {salonName}",
        
        // ========================================
        // ÉCRAN DE PARAMÈTRES
        // ========================================
        "settings.title" to "Paramètres",
        
        // Sections
        "settings.sections.account" to "Compte",
        "settings.sections.privacy" to "Confidentialité",
        "settings.sections.notifications" to "Notifications",
        "settings.sections.appearance" to "Apparence",
        "settings.sections.social_network" to "Réseau social",
        "settings.sections.bookings" to "Réservations",
        "settings.sections.content_and_media" to "Contenu et médias",
        "settings.sections.data_privacy" to "Confidentialité des données",
        "settings.sections.help_and_support" to "Aide et support",
        "settings.sections.about" to "À propos",
        
        // Compte
        "settings.account.profile" to "Profil",
        "settings.account.profile_subtitle" to "Gérer votre profil et vos informations",
        "settings.account.security" to "Sécurité",
        "settings.account.security_subtitle" to "Mot de passe, authentification à deux facteurs",
        "settings.account.email" to "Email",
        "settings.account.phone" to "Téléphone",
        "settings.account.phone_subtitle" to "Ajouter un numéro de téléphone",
        "settings.account.not_defined" to "Non défini",
        
        // Confidentialité
        "settings.privacy.profile_visibility" to "Visibilité du profil",
        "settings.privacy.who_can_follow_me" to "Qui peut me suivre",
        "settings.privacy.who_can_message_me" to "Qui peut m'envoyer des messages",
        "settings.privacy.show_activity_status" to "Afficher le statut d'activité",
        "settings.privacy.show_activity_status_subtitle" to "Les autres utilisateurs verront quand vous êtes en ligne",
        "settings.privacy.blocked_users" to "Utilisateurs bloqués",
        "settings.privacy.blocked_users_count" to "{count} utilisateur(s)",
        "settings.privacy.no_blocked_users" to "Aucun utilisateur bloqué",
        "settings.privacy.options.public" to "Public",
        "settings.privacy.options.followers_only" to "Abonnés uniquement",
        "settings.privacy.options.private" to "Privé",
        "settings.privacy.options.everyone" to "Tout le monde",
        "settings.privacy.options.nobody" to "Personne",
        
        // Notifications
        "settings.notifications.title" to "Notifications",
        "settings.notifications.subtitle" to "Activer ou désactiver toutes les notifications",
        "settings.notifications.push" to "Notifications push",
        "settings.notifications.push_subtitle" to "Recevoir des notifications sur votre appareil",
        "settings.notifications.email" to "Notifications par email",
        "settings.notifications.email_subtitle" to "Recevoir des notifications par email",
        "settings.notifications.bookings" to "Réservations",
        "settings.notifications.bookings_subtitle" to "Notifications pour vos rendez-vous",
        "settings.notifications.social" to "Réseau social",
        "settings.notifications.social_subtitle" to "Likes, commentaires, mentions, etc.",
        "settings.notifications.marketing" to "Marketing",
        "settings.notifications.marketing_subtitle" to "Offres spéciales et nouveautés",
        
        // Apparence
        "settings.appearance.dark_mode" to "Mode sombre",
        "settings.appearance.dark_mode_subtitle" to "Activer le thème sombre",
        "settings.appearance.language" to "Langue",
        
        // Réseau social
        "settings.social_network.post_visibility_default" to "Visibilité par défaut des posts",
        "settings.social_network.allow_comments" to "Autoriser les commentaires",
        "settings.social_network.allow_comments_subtitle" to "Les autres utilisateurs peuvent commenter vos posts",
        "settings.social_network.allow_reactions" to "Autoriser les réactions",
        "settings.social_network.allow_reactions_subtitle" to "Les autres utilisateurs peuvent réagir à vos posts",
        "settings.social_network.allow_shares" to "Autoriser le partage",
        "settings.social_network.allow_shares_subtitle" to "Les autres utilisateurs peuvent partager vos posts",
        
        // Réservations
        "settings.bookings.booking_notifications" to "Notifications de rendez-vous",
        "settings.bookings.booking_notifications_subtitle" to "Recevoir des notifications pour vos rendez-vous",
        "settings.bookings.availability_preferences" to "Préférences de disponibilité",
        "settings.bookings.availability_preferences_subtitle" to "Gérer vos créneaux préférés",
        "settings.bookings.payment_methods" to "Méthodes de paiement",
        "settings.bookings.payment_methods_subtitle" to "Gérer vos moyens de paiement",
        
        // Contenu et médias
        "settings.content_and_media.auto_save_photos" to "Enregistrer automatiquement les photos",
        "settings.content_and_media.auto_save_photos_subtitle" to "Sauvegarder les photos dans votre galerie",
        "settings.content_and_media.data_usage" to "Utilisation des données",
        "settings.content_and_media.video_quality" to "Qualité vidéo",
        "settings.content_and_media.data_usage_options.economical" to "Économique",
        "settings.content_and_media.data_usage_options.standard" to "Standard",
        "settings.content_and_media.data_usage_options.high" to "Élevée",
        "settings.content_and_media.video_quality_options.sd" to "SD",
        "settings.content_and_media.video_quality_options.hd" to "HD",
        "settings.content_and_media.video_quality_options.full_hd" to "Full HD",
        
        // Confidentialité des données
        "settings.data_privacy.download_data" to "Télécharger vos données",
        "settings.data_privacy.download_data_subtitle" to "Obtenir une copie de vos données",
        "settings.data_privacy.delete_account" to "Supprimer votre compte",
        "settings.data_privacy.delete_account_subtitle" to "Supprimer définitivement votre compte et toutes vos données",
        
        // Aide et support
        "settings.help_and_support.help_center" to "Centre d'aide",
        "settings.help_and_support.help_center_subtitle" to "FAQ et guides d'utilisation",
        "settings.help_and_support.contact_us" to "Nous contacter",
        "settings.help_and_support.contact_us_subtitle" to "Signaler un problème ou poser une question",
        "settings.help_and_support.report_bug" to "Signaler un bug",
        "settings.help_and_support.report_bug_subtitle" to "Aidez-nous à améliorer l'application",
        "settings.help_and_support.rate_app" to "Évaluer l'application",
        "settings.help_and_support.rate_app_subtitle" to "Donnez votre avis sur le store",
        
        // À propos
        "settings.about.version" to "Version",
        "settings.about.terms_of_service" to "Conditions d'utilisation",
        "settings.about.terms_of_service_subtitle" to "Lire les conditions d'utilisation",
        "settings.about.privacy_policy" to "Politique de confidentialité",
        "settings.about.privacy_policy_subtitle" to "Lire la politique de confidentialité",
        "settings.about.licenses" to "Licences",
        "settings.about.licenses_subtitle" to "Licences open source",
        
        // Actions
        "settings.actions.logout" to "Déconnexion",
        "settings.actions.logout_dialog_title" to "Déconnexion",
        "settings.actions.logout_dialog_text" to "Êtes-vous sûr de vouloir vous déconnecter ?",
        "settings.actions.delete" to "Supprimer",
        "settings.actions.delete_account_dialog_title" to "Supprimer votre compte",
        "settings.actions.delete_account_dialog_text" to "Cette action est irréversible. Toutes vos données seront définitivement supprimées. Êtes-vous absolument sûr ?",
        "settings.actions.cancel" to "Annuler",
        
        // ========================================
        // ÉCRAN DE PROFIL
        // ========================================
        "profile.version" to "Version 1.0.0",
        "profile.logout_dialog_title" to "Déconnexion",
        "profile.logout_dialog_text" to "Êtes-vous sûr de vouloir vous déconnecter ?",
        "profile.logout_confirm" to "Oui, me déconnecter",
        "profile.cancel" to "Annuler",
        "profile.avatar_preview" to "Aperçu avatar",
        "profile.save" to "Enregistrer",
        "profile.edit_photo" to "Modifier la photo",
        "profile.account_verified" to "Compte vérifié",
        "profile.account_not_verified" to "Non vérifié",
        "profile.account_info" to "Informations du compte",
        "profile.email" to "Email",
        "profile.phone" to "Téléphone",
        "profile.member_since" to "Membre depuis",
        "profile.account_type" to "Type de compte",
        "profile.edit_profile" to "Modifier mon profil",
        "profile.edit_profile_subtitle" to "Changer nom, prénom, téléphone",
        "profile.change_password" to "Changer le mot de passe",
        "profile.change_password_subtitle" to "Mettre à jour vos identifiants",
        "profile.logout" to "Se déconnecter",
        "profile.logout_subtitle" to "Quitter votre session",
        "profile.statistics" to "Statistiques",
        "profile.salons" to "Salons",
        "profile.bookings" to "Réservations",
        "profile.reviews" to "Avis",
        "profile.services" to "Services",
        "profile.points" to "Points",
        "profile.user_types.client" to "Client",
        "profile.user_types.salon_owner" to "Propriétaire de salon",
        "profile.user_types.hairstylist" to "Coiffeur",
        "profile.user_types.admin" to "Administrateur",
        
        // ========================================
        // ÉCRAN DE FIL D'ACTUALITÉ SOCIAL
        // ========================================
        "social_feed.filter_by_type" to "Filtrer par type",
        "social_feed.my_follows" to "Mes suivis",
        "social_feed.near_me" to "Près de moi",
        "social_feed.all" to "Tous",
        "social_feed.add_comment" to "Ajoutez un commentaire (optionnel)",
        "social_feed.share" to "Partager",
        "social_feed.cancel" to "Annuler",
        "social_feed.no_collection_yet" to "Vous n'avez pas encore de collection",
        "social_feed.save" to "Enregistrer",
        "social_feed.archive" to "Archiver",
        "social_feed.share_in_app" to "Partager dans l'app",
        "social_feed.share_external" to "Partager vers l'extérieur",
        "social_feed.report" to "Signaler",
        "social_feed.add_to_collection_example" to "Ex: J'ai réservé chez ce salon grâce à ce post !",
        "social_feed.add_to_collection" to "Ajouter à une collection",
        "social_feed.create_collection" to "Créer une collection",
        
        // ========================================
        // ÉCRAN MES RÉSERVATIONS
        // ========================================
        "my_bookings.service" to "Service",
        "my_bookings.date_time" to "Date & Heure",
        "my_bookings.hairstylist" to "Coiffeur",
        "my_bookings.amount" to "Montant",
        "my_bookings.review_left" to "Avis laissé",
        "my_bookings.leave_review" to "Laisser un avis",
        "my_bookings.cancel" to "Annuler",
        "my_bookings.cancel_confirm" to "Oui, annuler",
        "my_bookings.cancel_keep" to "Non, garder",
        
        // ========================================
        // ÉCRAN DE RÉSERVATION (WIZARD)
        // ========================================
        "booking.back_to_home" to "Retour à l'accueil",
        "booking.duration" to "Durée",
        "booking.category" to "Catégorie",
        "booking.choose_expert" to "Choisissez votre expert",
        "booking.choose_expert_subtitle" to "Sélectionnez un coiffeur ou laissez le salon choisir",
        "booking.select_date" to "Sélectionnez une date",
        "booking.select_date_subtitle" to "Choisissez le jour de votre rendez-vous",
        "booking.choose_time_slot" to "Choisissez un créneau",
        "booking.choose_time_slot_subtitle" to "Sélectionnez l'horaire qui vous convient",
        "booking.notes_or_special_requests" to "Notes ou demandes spéciales",
        "booking.notes_or_special_requests_subtitle" to "Facultatif - Informez-nous de vos préférences",
        "booking.notes_info" to "Ces informations seront transmises à votre coiffeur",
        "booking.expert_selected" to "Expert sélectionné",
        "booking.continue" to "Continuer",
        "booking.time_slot_selected" to "Créneau sélectionné",
        "booking.pay_now" to "Payer maintenant",
        "booking.view_my_bookings" to "Voir mes réservations",
        
        // ========================================
        // ÉCRAN DE DÉTAIL DE RÉSERVATION
        // ========================================
        "booking_detail.cancel_keep" to "Non, garder",
        "booking_detail.hair_salon" to "Salon de Coiffure",
        "booking_detail.click_for_details" to "Cliquez pour voir les détails",
        "booking_detail.service" to "Service",
        "booking_detail.service_details" to "Détails de votre prestation",
        "booking_detail.booking" to "Réservation",
        "booking_detail.booking_details" to "Date, heure et coiffeur",
        "booking_detail.payment" to "Paiement",
        "booking_detail.payment_details" to "Détails de la transaction",
        "booking_detail.amount" to "MONTANT",
        "booking_detail.cancel_booking_title" to "Annuler la réservation ?",
        "booking_detail.cancel_confirm_message" to "Êtes-vous sûr de vouloir annuler cette réservation ? Cette action est irréversible.",
        "booking_detail.booking_status" to "État de votre réservation",
        
        // ========================================
        // ÉCRAN DE DÉTAIL DE SALON
        // ========================================
        "salon_detail.services" to "Services",
        "salon_detail.view_posts" to "Voir les posts",
        "salon_detail.open_feed" to "Ouvrir le feed",
        "salon_detail.add_member" to "Ajouter un membre",
        "salon_detail.queue" to "File d'attente",
        "salon_detail.position" to "Position: #{position}",
        "salon_detail.leave_queue" to "Quitter la file",
        "salon_detail.join_queue" to "Rejoindre la file",
        "salon_detail.login_to_join_queue" to "Connectez-vous pour rejoindre la file",
        "salon_detail.cancel" to "Annuler",
        "salon_detail.subscribers" to "Abonnés",
        "salon_detail.book" to "Réserver",
        "salon_detail.salon_verified" to "Salon vérifié",
        "salon_detail.clients" to "Clients",
        "salon_detail.waiting" to "Attente",
        "salon_detail.your_position" to "Votre position",
        "salon_detail.position_in_queue" to "Numéro {position} dans la file",
        "salon_detail.my_team" to "Mon Équipe",
        "salon_detail.team" to "Équipe",
        "salon_detail.our_services" to "Nos Prestations",
        "salon_detail.choose_from_services" to "Choisissez parmi {count} service",
        "salon_detail.choose_from_services_plural" to "Choisissez parmi {count} services",
        "salon_detail.loading_services" to "Chargement des services",
        "salon_detail.please_wait" to "Veuillez patienter...",
        "salon_detail.loading_error" to "Erreur de chargement",
        "salon_detail.no_services_available" to "Aucune prestation disponible",
        "salon_detail.no_services_message" to "Ce salon n'a pas encore ajouté de services.\nRevenez plus tard !",
        "salon_detail.ready_to_book" to "Prêt à réserver ?",
        "salon_detail.reviews_and_ratings" to "Avis & Notes",
        
        // ========================================
        // ÉCRAN DE CRÉATION DE SALON
        // ========================================
        "create_salon.launch_your_salon" to "Lancez votre salon",
        "create_salon.join_community" to "Rejoignez notre communauté de professionnels",
        "create_salon.salon_info" to "Informations du salon",
        "create_salon.cover_photo" to "Photo de couverture",
        "create_salon.cover_photo_hint" to "Optionnel • JPG, PNG • Max 10MB",
        "create_salon.add_photo" to "Ajouter une photo",
        "create_salon.click_to_browse" to "Cliquez pour parcourir vos fichiers",
        "create_salon.data_secured" to "Vos données sont sécurisées et protégées",
        
        // ========================================
        // ÉCRAN DE DÉTAIL DE POST
        // ========================================
        "post_detail.comments" to "{count} commentaire",
        "post_detail.comments_plural" to "{count} commentaires",
        
        // ========================================
        // ÉCRAN DE CRÉATION DE POST
        // ========================================
        "create_post.public" to "Public",
        "create_post.post_type" to "Type de post",
        "create_post.visibility" to "Visibilité",
        "create_post.at_least_two_images" to "Au moins 2 images (Avant + Après)",
        "create_post.click_to_select_photo" to "Cliquez pour sélectionner une photo",
        "create_post.photo_format_hint" to "JPG, PNG (max 10MB)",
        "create_post.post_will_be_visible" to "Votre post sera visible par tous les utilisateurs",
        
        // ========================================
        // ÉCRAN DE PROFIL COIFFEUR
        // ========================================
        "coiffeur_profile.pinned_posts" to "Posts épinglés",
        "coiffeur_profile.recent_posts" to "Posts récents",
        "coiffeur_profile.unfollow" to "Ne plus suivre",
        "coiffeur_profile.follow" to "Suivre",
        "coiffeur_profile.badges_and_certifications" to "Badges et Certifications",
        "coiffeur_profile.featured_portfolio" to "Portfolio mis en avant",
        "coiffeur_profile.portfolios" to "Portfolios",
        
        // ========================================
        // ÉCRAN DE PROFIL SOCIAL SALON
        // ========================================
        "salon_social_profile.recent_posts" to "Posts récents",
        "salon_social_profile.unfollow" to "Ne plus suivre",
        "salon_social_profile.follow" to "Suivre",
        "salon_social_profile.featured_posts" to "Posts mis en avant",
        "salon_social_profile.portfolios" to "Portfolios",
        "salon_social_profile.verified" to "Vérifié",
        "salon_social_profile.services" to "Services",
        
        // ========================================
        // ÉCRAN DE PROFIL CLIENT
        // ========================================
        "client_profile.title" to "Profil Client",
        "client_profile.about" to "À propos",
        "client_profile.posts" to "Posts",
        "client_profile.likes" to "J'aime",
        "client_profile.followers" to "Abonnés",
        "client_profile.following" to "Abonnements",
        "client_profile.collections" to "Collections",
        "client_profile.collections_count" to "Collections",
        "client_profile.recent_posts" to "Posts récents",
        "client_profile.badges" to "Badges",
        "client_profile.follow" to "Suivre",
        "client_profile.unfollow" to "Ne plus suivre",
        
        // ========================================
        // ÉCRAN DE PROFIL PROPRIÉTAIRE DE SALON
        // ========================================
        "salon_owner_profile.title" to "Profil Propriétaire",
        "salon_owner_profile.about" to "À propos",
        "salon_owner_profile.posts" to "Posts",
        "salon_owner_profile.likes" to "J'aime",
        "salon_owner_profile.followers" to "Abonnés",
        "salon_owner_profile.salons" to "Salons",
        "salon_owner_profile.collections" to "Collections",
        "salon_owner_profile.collections_count" to "Collections",
        "salon_owner_profile.recent_posts" to "Posts récents",
        "salon_owner_profile.badges" to "Badges",
        "salon_owner_profile.follow" to "Suivre",
        "salon_owner_profile.unfollow" to "Ne plus suivre",
        
        // ========================================
        // ÉCRAN DE COMMENTAIRES
        // ========================================
        "comments.title" to "Commentaires",
        "comments.add_comment" to "Ajouter un commentaire...",
        "comments.load_more" to "Charger plus de commentaires",
        
        // ========================================
        // ÉCRAN DE COLLECTIONS
        // ========================================
        "collections.title" to "Collections",
        "collections.post" to "post",
        "collections.posts" to "posts",
        "collections.edit" to "Modifier",
        "collections.delete" to "Supprimer",
        "collections.new_collection" to "Nouvelle collection",
        "collections.name" to "Nom *",
        "collections.description" to "Description (optionnel)",
        "collections.category" to "Catégorie",
        "collections.public_collection" to "Collection publique",
        "collections.save_to_collection" to "Sauvegarder dans une collection",
        "collections.no_collections" to "Aucune collection. Créez-en une d'abord !",
        
        // ========================================
        // ÉCRAN DE DÉTAIL DE COLLECTION
        // ========================================
        "collection_detail.title" to "Collection",
        "collection_detail.load_more" to "Charger plus",
        "collection_detail.edit" to "Modifier",
        "collection_detail.delete" to "Supprimer",
        "collection_detail.delete_collection" to "Supprimer la collection",
        "collection_detail.delete_collection_message" to "Êtes-vous sûr de vouloir supprimer cette collection ? Tous les posts seront retirés de la collection.",
        "collection_detail.remove_from_collection" to "Retirer de la collection",
        
        // ========================================
        // ÉCRAN DE CRÉATION DE PORTFOLIO
        // ========================================
        "create_portfolio.title" to "Créer un portfolio",
        "create_portfolio.no_salon_found" to "Aucun salon trouvé. Veuillez créer un salon d'abord.",
        "create_portfolio.salon" to "Salon",
        "create_portfolio.cover_image" to "Image de couverture",
        "create_portfolio.add_cover_image" to "Ajouter une image de couverture",
        "create_portfolio.public_portfolio" to "Portfolio public",
        "create_portfolio.name" to "Nom du portfolio *",
        "create_portfolio.name_placeholder" to "Ex: Mes Colorations 2024",
        "create_portfolio.description" to "Description",
        
        // ========================================
        // ÉCRAN DE DÉTAIL DE PORTFOLIO
        // ========================================
        "portfolio_detail.load_more" to "Charger plus",
        
        // ========================================
        // ÉCRAN DE LISTE DE PORTFOLIOS
        // ========================================
        "portfolios_list.create_portfolio" to "Créer un portfolio",
        
        // ========================================
        // ÉCRAN DE CRÉATION DE SERVICE
        // ========================================
        "create_service.premium_service" to "Service premium",
        "create_service.service_info" to "Informations du service",
        "create_service.category" to "Catégorie *",
        "create_service.price_adjustment_info" to "Les prix peuvent être ajustés à tout moment depuis votre espace professionnel.",
        "create_service.name" to "Nom du service *",
        "create_service.name_placeholder" to "Ex: Coupe Homme, Coloration complète...",
        "create_service.description" to "Description",
        "create_service.duration" to "Durée (min) *",
        "create_service.price" to "Prix (€) *",
        
        // ========================================
        // ÉCRAN DE POSTS DE SALON
        // ========================================
        "salon_posts.title" to "Posts de {salonName}",
        "salon_posts.all" to "Tous",
        "salon_posts.all_services" to "Tous les services",
        "salon_posts.popular" to "Populaires",
        
        // ========================================
        // ÉCRAN D'ARCHIVES
        // ========================================
        "archives.title" to "Mes Archives",
        
        // ========================================
        // ÉCRAN DE FAVORIS
        // ========================================
        "favorites.title" to "Mes Favoris",
        "favorites.offline_mode" to "Mode hors ligne - Données en cache",
        
        // ========================================
        // ÉCRAN DE TENDANCES
        // ========================================
        "trending.title" to "Tendances",
        "trending.posts" to "Posts",
        "trending.hashtags" to "Hashtags",
        "trending.salons" to "Salons",
        
        // ========================================
        // ÉCRAN DE SIGNALEMENT
        // ========================================
        "report.info_message" to "Votre signalement sera examiné par notre équipe de modération. Merci de nous aider à maintenir une communauté respectueuse.",
        "report.reason_title" to "Raison du signalement *",
        "report.additional_info" to "Informations supplémentaires (optionnel)",
        "report.cancel" to "Annuler",
        "report.submit" to "Signaler",
        "report.reported_content" to "Contenu signalé",
        "report.post_author" to "Auteur du post",
        
        // ========================================
        // ÉCRAN DE CRÉATION DE STAFF
        // ========================================
        "create_staff.title" to "Ajouter un membre",
        "create_staff.new_collaborator" to "Nouveau collaborateur",
        "create_staff.personal_info" to "Informations personnelles",
        "create_staff.specialties" to "Spécialités",
        "create_staff.specialties_hint" to "Sélectionnez les catégories de services que ce membre peut effectuer.",
        "create_staff.email_info" to "Le collaborateur recevra un email avec ses informations de connexion.",
        "create_staff.first_name" to "Prénom *",
        "create_staff.first_name_placeholder" to "Ex: Jean",
        "create_staff.last_name" to "Nom *",
        "create_staff.last_name_placeholder" to "Ex: Dupont",
        "create_staff.email" to "Email *",
        
        // ========================================
        // ÉCRAN DE GESTION DE FILE D'ATTENTE
        // ========================================
        "queue_management.title" to "{salonName} - Gestion de la file",
        "queue_management.ticket" to "Ticket #{position}",
        "queue_management.arrived" to "Arrivé",
        "queue_management.estimated_wait" to "Attente estimée",
        "queue_management.call_next" to "Au suivant",
        "queue_management.remove" to "Retirer",
        
        // ========================================
        // ÉCRAN CHANGEMENT EMAIL
        // ========================================
        "change_email.title" to "Modifier l'email",
        "change_email.current_email" to "Email actuel",
        "change_email.new_email_section" to "Nouvel email",
        "change_email.new_email" to "Nouvel email",
        "change_email.confirm_with_password" to "Confirmer avec votre mot de passe",
        "change_email.save" to "Enregistrer",
        "change_email.saving" to "Enregistrement...",
        "change_email.all_fields_required" to "Veuillez remplir tous les champs",
        "change_email.invalid_email" to "Adresse email invalide",
        "change_email.error" to "Erreur lors du changement d'email",
        "change_email.info" to "Votre nouvel email sera utilisé pour vous connecter à votre compte.",
        
        // ========================================
        // ÉCRAN CHANGEMENT TÉLÉPHONE
        // ========================================
        "change_phone.title" to "Modifier le téléphone",
        "change_phone.current_phone" to "Téléphone actuel",
        "change_phone.not_defined" to "Non défini",
        "change_phone.new_phone_section" to "Nouveau numéro",
        "change_phone.new_phone" to "Nouveau numéro de téléphone",
        "change_phone.phone_placeholder" to "+33 6 12 34 56 78",
        "change_phone.leave_blank_to_remove" to "Laissez vide pour supprimer",
        "change_phone.confirm_with_password" to "Confirmer avec votre mot de passe",
        "change_phone.save" to "Enregistrer",
        "change_phone.saving" to "Enregistrement...",
        "change_phone.password_required" to "Le mot de passe est obligatoire",
        "change_phone.error" to "Erreur lors du changement de téléphone",
        
        // ========================================
        // ÉCRAN DE SÉCURITÉ
        // ========================================
        "security.title" to "Sécurité",
        "security.change_password" to "Changer le mot de passe",
        "security.current_password" to "Mot de passe actuel",
        "security.new_password" to "Nouveau mot de passe",
        "security.confirm_password" to "Confirmer le mot de passe",
        "security.password_requirements" to "Au moins 8 caractères",
        "security.change_password_button" to "Changer le mot de passe",
        "security.changing" to "Modification...",
        "security.all_fields_required" to "Tous les champs sont obligatoires",
        "security.passwords_do_not_match" to "Les mots de passe ne correspondent pas",
        "security.password_too_short" to "Le mot de passe doit contenir au moins 8 caractères",
        "security.change_password_error" to "Erreur lors du changement de mot de passe",
        "security.password_changed" to "Mot de passe changé avec succès",
        "security.active_sessions" to "Sessions actives",
        "security.no_active_sessions" to "Aucune session active",
        "security.current_session" to "Cette session",
        "security.other_session" to "Autre appareil",
        "security.current_badge" to "Actuel",
        "security.created_at" to "Créée le",
        "security.expires_at" to "Expire le",
        "security.revoke" to "Révoquer",
        "security.revoke_all" to "Tout révoquer",
        "security.revoke_session_title" to "Révoquer la session",
        "security.revoke_session_message" to "Voulez-vous vraiment déconnecter cet appareil ?",
        "security.revoke_all_sessions" to "Déconnecter tous les appareils",
        "security.revoke_all_sessions_title" to "Déconnecter tous les appareils",
        "security.revoke_all_sessions_message" to "Voulez-vous vraiment déconnecter tous les autres appareils ? Vous devrez vous reconnecter sur chacun d'eux.",
        "security.session_revoked" to "Session révoquée avec succès",
        "security.all_sessions_revoked" to "Toutes les autres sessions ont été révoquées",
        "security.revoke_session_error" to "Erreur lors de la révocation de la session",
        "security.revoke_all_sessions_error" to "Erreur lors de la révocation des sessions",
        "security.security_tip" to "Conseil : Changez régulièrement votre mot de passe et déconnectez les appareils que vous ne reconnaissez pas.",
        
        // Indicateur de force du mot de passe
        "security.password_strength" to "Force du mot de passe",
        "security.password_weak" to "Faible",
        "security.password_fair" to "Moyen",
        "security.password_good" to "Bon",
        "security.password_strong" to "Fort",
        
        // Statut de sécurité
        "security.security_status" to "Statut de sécurité",
        "security.account_protected" to "Votre compte est protégé",
        "security.active_devices" to "Appareils actifs",
        "security.password_set" to "Mot de passe défini",
        
        // Mises à jour en temps réel
        "security.real_time_updates" to "Mise à jour en temps réel",
        
        // Conseils de sécurité améliorés
        "security.security_tips_title" to "Conseils de sécurité",
        "security.tip_1" to "Utilisez un mot de passe unique d'au moins 12 caractères avec des majuscules, minuscules, chiffres et symboles.",
        "security.tip_2" to "Ne partagez jamais votre mot de passe et activez l'authentification à deux facteurs si disponible.",
        "security.tip_3" to "Vérifiez régulièrement vos sessions actives et déconnectez tout appareil suspect.",
        
        // ========================================
        // SUPPRESSION DE COMPTE
        // ========================================
        "delete_account.title" to "Supprimer le compte",
        "delete_account.warning" to "Cette action est irréversible. Toutes vos données seront définitivement supprimées.",
        "delete_account.confirm_title" to "Supprimer votre compte ?",
        "delete_account.confirm_message" to "Cette action supprimera définitivement votre compte et toutes vos données (réservations, avis, photos, etc.). Cette action est irréversible.",
        "delete_account.password_label" to "Entrez votre mot de passe pour confirmer",
        "delete_account.confirm_checkbox" to "Je comprends que cette action est irréversible",
        "delete_account.delete_button" to "Supprimer mon compte",
        "delete_account.deleting" to "Suppression en cours...",
        "delete_account.success" to "Votre compte a été supprimé avec succès",
        "delete_account.error" to "Erreur lors de la suppression du compte",
        
        // ========================================
        // ÉCRAN MÉTHODES DE PAIEMENT
        // ========================================
        "payment_methods.title" to "Méthodes de paiement",
        "payment_methods.no_cards" to "Aucune carte enregistrée",
        "payment_methods.description" to "Vos cartes de paiement seront enregistrées de manière sécurisée lors de votre prochain achat.",
        "payment_methods.stripe_info" to "Vos informations de paiement sont sécurisées par Stripe. Nous ne stockons jamais vos numéros de carte.",
        "payment_methods.add_card" to "Ajouter une carte",
        
        // ========================================
        // ÉCRAN UTILISATEURS BLOQUÉS
        // ========================================
        "blocked_users.title" to "Utilisateurs bloqués",
        "blocked_users.no_blocked_users" to "Aucun utilisateur bloqué",
        "blocked_users.description" to "Les utilisateurs que vous bloquez ne pourront plus voir votre profil ni vous contacter.",
        "blocked_users.unblock" to "Débloquer",
        
        // ========================================
        // ÉCRAN CENTRE D'AIDE
        // ========================================
        "help_center.title" to "Centre d'aide",
        "help_center.faq_title" to "Questions fréquentes",
        "help_center.need_more_help" to "Vous n'avez pas trouvé de réponse ?",
        "help_center.contact_support" to "Contacter le support",
        "help_center.faq1_question" to "Comment annuler une réservation ?",
        "help_center.faq1_answer" to "Rendez-vous dans 'Mes réservations', sélectionnez la réservation concernée et appuyez sur 'Annuler'. L'annulation est gratuite jusqu'à 24h avant le rendez-vous.",
        "help_center.faq2_question" to "Comment modifier mon profil ?",
        "help_center.faq2_answer" to "Allez dans Paramètres > Mon compte > Modifier le profil pour mettre à jour vos informations personnelles.",
        "help_center.faq3_question" to "Comment contacter un salon ?",
        "help_center.faq3_answer" to "Sur la page du salon, vous trouverez les coordonnées de contact : téléphone, email et adresse.",
        "help_center.faq4_question" to "Les paiements sont-ils sécurisés ?",
        "help_center.faq4_answer" to "Oui, tous les paiements sont traités par Stripe, leader mondial du paiement en ligne. Vos données bancaires ne sont jamais stockées sur nos serveurs.",
        
        // ========================================
        // ÉCRAN CONTACTER LE SUPPORT
        // ========================================
        "contact.title" to "Contacter le support",
        "contact.response_time" to "Nous répondons généralement sous 24-48h.",
        "contact.send_message" to "Envoyer un message",
        "contact.subject" to "Sujet",
        "contact.message" to "Votre message",
        "contact.send" to "Envoyer",
        "contact.sending" to "Envoi en cours...",
        
        // ========================================
        // CONDITIONS D'UTILISATION
        // ========================================
        "terms.title" to "Conditions d'utilisation",
        "terms.last_update" to "Dernière mise à jour : janvier 2025",
        "terms.section1_title" to "1. Acceptation des conditions",
        "terms.section1_content" to "En utilisant l'application Frollot, vous acceptez les présentes conditions d'utilisation. Si vous n'acceptez pas ces conditions, veuillez ne pas utiliser l'application.",
        "terms.section2_title" to "2. Description du service",
        "terms.section2_content" to "Frollot est une plateforme de mise en relation entre les salons de coiffure et leurs clients. Elle permet la réservation en ligne, le paiement et le suivi des rendez-vous.",
        "terms.section3_title" to "3. Compte utilisateur",
        "terms.section3_content" to "Vous êtes responsable de la confidentialité de vos identifiants de connexion et de toutes les activités effectuées sous votre compte.",
        "terms.section4_title" to "4. Paiements et annulations",
        "terms.section4_content" to "Les paiements sont traités de manière sécurisée via Stripe. Les conditions d'annulation varient selon chaque salon.",
        
        // ========================================
        // POLITIQUE DE CONFIDENTIALITÉ
        // ========================================
        "privacy.title" to "Politique de confidentialité",
        "privacy.last_update" to "Dernière mise à jour : janvier 2025",
        "privacy.section1_title" to "1. Données collectées",
        "privacy.section1_content" to "Nous collectons les données que vous nous fournissez (nom, email, téléphone) ainsi que les données d'utilisation de l'application pour améliorer nos services.",
        "privacy.section2_title" to "2. Utilisation des données",
        "privacy.section2_content" to "Vos données sont utilisées pour gérer vos réservations, vous envoyer des notifications importantes et améliorer votre expérience utilisateur.",
        "privacy.section3_title" to "3. Partage des données",
        "privacy.section3_content" to "Nous partageons vos informations de réservation avec les salons concernés. Nous ne vendons jamais vos données personnelles à des tiers.",
        "privacy.section4_title" to "4. Vos droits",
        "privacy.section4_content" to "Vous avez le droit d'accéder, de modifier ou de supprimer vos données personnelles à tout moment via les paramètres de votre compte.",
        
        // ========================================
        // ÉCRAN DE PAIEMENT
        // ========================================
        "payment.title" to "Paiement",
        "payment.cancel" to "Annuler",
        "payment.service" to "Service:",
        "payment.salon" to "Salon:",
        "payment.date" to "Date:",
        
        // ========================================
        // ÉCRAN DE DEMANDE DE VÉRIFICATION
        // ========================================
        "request_verification.title" to "Demander une vérification",
        "request_verification.header_title" to "Demande de vérification",
        "request_verification.description" to "Sélectionnez le type de vérification que vous souhaitez obtenir. Notre équipe examinera votre demande sous peu.",
        "request_verification.verification_type" to "Type de vérification *",
        "request_verification.additional_info" to "Informations supplémentaires (optionnel)",
        "request_verification.additional_info_placeholder" to "Décrivez votre situation, fournissez des documents (SIRET, diplômes, etc.)...",
        
        // ========================================
        // ÉCRAN DE CRÉATION D'AVIS
        // ========================================
        "create_review.title" to "Laisser un avis",
        "create_review.your_booking" to "Votre réservation",
        "create_review.salon" to "Salon: {salonName}",
        "create_review.date" to "Date: {date}",
        "create_review.rating_question" to "Comment évaluez-vous votre expérience ?",
        "create_review.title_label" to "Titre de votre avis (optionnel)",
        "create_review.title_placeholder" to "Ex: Super expérience !",
        "create_review.comment_label" to "Votre commentaire (optionnel)",
        "create_review.comment_placeholder" to "Partagez votre expérience...",
        
        // ========================================
        // ÉCRAN DE GESTION DES RENDEZ-VOUS PROPRIÉTAIRE
        // ========================================
        "owner_bookings_management.title" to "Gestion des Rendez-vous",
        "owner_bookings_management.confirm" to "Confirmer",
        "owner_bookings_management.start" to "Démarrer",
        "owner_bookings_management.absent" to "Absent",
        "owner_bookings_management.finish" to "Terminer",
        
        // ========================================
        // ÉCRAN DE RECHERCHE
        // ========================================
        "search.title" to "Recherche",
        "search.advanced_filters" to "Filtres avancés",
        "search.post_type" to "Type de post",
        "search.search_placeholder" to "Recherchez des posts, salons, utilisateurs...",
        "search.no_posts_found" to "Aucun post trouvé",
        "search.no_salons_found" to "Aucun salon trouvé",
        "search.no_users_found" to "Aucun utilisateur trouvé",
        "search.no_hashtags_found" to "Aucun hashtag trouvé",
        "search.no_results_found" to "Aucun résultat trouvé",
        "search.posts" to "Posts ({count})",
        "search.salons" to "Salons ({count})",
        "search.users" to "Utilisateurs ({count})",
        "search.hashtags" to "Hashtags ({count})",
        "search.all" to "Tous",
        "search.load_more" to "Charger plus",
        
        // ========================================
        // COMPOSANT ULTRA PREMIUM POST CARD
        // ========================================
        "ultra_premium_post_card.oops" to "Oups !",
        "ultra_premium_post_card.retry" to "Réessayer",
        "ultra_premium_post_card.no_posts_yet" to "Aucun post pour le moment",
        "ultra_premium_post_card.be_first_to_share" to "Soyez le premier à partager\nquelque chose d'incroyable !",
        "ultra_premium_post_card.view_comments" to "Voir le commentaire",
        "ultra_premium_post_card.view_comments_plural" to "Voir les {count} commentaires",
        "ultra_premium_post_card.add_comment" to "Ajouter un commentaire...",
        "ultra_premium_post_card.pinned" to "Épinglé",
        
        // ========================================
        // BOUTONS ET ACTIONS COMMUNS
        // ========================================
        "common.cancel" to "Annuler",
        "common.save" to "Enregistrer",
        "common.confirm" to "Confirmer",
        "common.retry" to "Réessayer",
        "common.delete" to "Supprimer",
        "common.edit" to "Modifier",
        "common.remove" to "Retirer",
        "common.add" to "Ajouter",
        "common.create" to "Créer",
        "common.load_more" to "Charger plus",
        "common.all" to "Tous",
        "common.start" to "Démarrer",
        "common.finish" to "Terminer",
        "common.absent" to "Absent",
        "common.popular" to "Populaires",
        "common.public" to "Public",
        "common.service" to "Service:",
        "common.salon" to "Salon:",
        "common.date" to "Date:",
        "common.verified" to "Vérifié",
        
        // ========================================
        // COMPOSANTS UI
        // ========================================
        // RatingBar
        "components.rating_bar.star_filled" to "Étoile {number} remplie",
        "components.rating_bar.star_empty" to "Étoile {number} vide",
        "components.rating_bar.reviews_count" to "({count} avis)",
        
        // PasswordTextField
        "components.password_text_field.label" to "Mot de passe",
        "components.password_text_field.placeholder" to "Entrez votre mot de passe",
        "components.password_text_field.show_password" to "Afficher le mot de passe",
        "components.password_text_field.hide_password" to "Masquer le mot de passe",
        
        // ReportDialog
        "components.report_dialog.title" to "Signaler ce {entity}",
        "components.report_dialog.info_message" to "Votre signalement sera examiné par notre équipe de modération. Merci de nous aider à maintenir une communauté respectueuse.",
        "components.report_dialog.reason_label" to "Raison du signalement *",
        "components.report_dialog.additional_info_label" to "Informations supplémentaires (optionnel)",
        "components.report_dialog.additional_info_placeholder" to "Décrivez brièvement le problème...",
        "components.report_dialog.error_select_reason" to "Veuillez sélectionner une raison",
        "components.report_dialog.error_reporting" to "Erreur lors du signalement: {error}",
        "components.report_dialog.error_unknown" to "Erreur inconnue",
        
        // UserAvatar
        "components.user_avatar.content_description" to "Avatar de {name}",
        
        // QueueStatusCard
        "components.queue_status_card.connection_lost" to "Connexion perdue",
        "components.queue_status_card.data_stale" to "Données obsolètes",
        "components.queue_status_card.last_update" to "Dernière mise à jour : il y a {minutes} min",
        "components.queue_status_card.your_progress" to "Votre progression",
        "components.queue_status_card.current_position" to "Position actuelle",
        "components.queue_status_card.estimated_time" to "Temps estimé",
        "components.queue_status_card.just_now" to "À l'instant",
        "components.queue_status_card.minutes_ago" to "Il y a {minutes}min",
        "components.queue_status_card.leave_queue" to "Quitter la file",
        "components.queue_status_card.reconnecting" to "Reconnexion automatique en cours...",
        "components.queue_status_card.keep_app_open" to "Gardez l'application ouverte pour être notifié par le salon.",
        "components.queue_status_card.status_offline" to "❌ Hors ligne",
        "components.queue_status_card.status_pending" to "⏸️ Mise à jour en attente...",
        "components.queue_status_card.status_auto_refresh" to "✅ Actualisation auto (30s)",
        
        // FullScreenImageViewer
        "components.full_screen_image_viewer.image_content_description" to "Image {number}",
        "components.full_screen_image_viewer.close" to "Fermer",
        
        // AppDrawer
        "components.app_drawer.marketplace" to "Marketplace",
        "components.app_drawer.social" to "Social",
        "components.app_drawer.appointments" to "Rendez-vous",
        "components.app_drawer.account" to "Compte",
        "components.app_drawer.profile" to "Profil",
        "components.app_drawer.notifications" to "Notifications",
        "components.app_drawer.favorites" to "Favoris",
        "components.app_drawer.archives" to "Archives",
        "components.app_drawer.collections" to "Collections",
        "components.app_drawer.management" to "Gestion",
        "components.app_drawer.my_salons" to "Mes Salons",
        "components.app_drawer.new_salon" to "Nouveau Salon",
        "components.app_drawer.create_post" to "Créer un post",
        "components.app_drawer.bookings_management" to "Gestion Rendez-vous",
        "components.app_drawer.stats" to "Stats",
        "components.app_drawer.activity" to "Activité",
        "components.app_drawer.my_portfolios" to "Mes Portfolios",
        "components.app_drawer.new_portfolio" to "Nouveau Portfolio",
        "components.app_drawer.services" to "Prestations",
        "components.app_drawer.agenda" to "Agenda",
        "components.app_drawer.admin" to "Admin",
        "components.app_drawer.dashboard" to "Dashboard",
        "components.app_drawer.users" to "Utilisateurs",
        "components.app_drawer.settings" to "Paramètres",
        "components.app_drawer.help" to "Aide",
        "components.app_drawer.guest" to "Invité",
        
        // ========================================
        // ÉCRAN DE PAIEMENT
        // ========================================
        "payment.title" to "Paiement",
        "payment.cancel" to "Annuler",
        "payment.service" to "Service",
        "payment.salon" to "Salon",
        "payment.date" to "Date",
        "payment.card_input" to "Carte",
        "payment.confirmation" to "Vérification",
        "payment.processing" to "Traitement...",
        "payment.success" to "Paiement réussi",
        "payment.error" to "Erreur",
        "payment.continue" to "Continuer",
        "payment.retry" to "Réessayer",
        "payment.pay_amount" to "Payer {amount}€",
        "payment.verify_order" to "Vérifiez votre commande",
        "payment.booking_details" to "Détails de la réservation",
        "payment.card" to "Carte",
        "payment.total_to_pay" to "Total à payer",
        "payment.modify_card" to "Modifier la carte",
        "payment.processing_message" to "Veuillez patienter pendant que nous traitons votre paiement de manière sécurisée.",
        "payment.success_message" to "Votre réservation est confirmée. Vous recevrez un email de confirmation.",
        "payment.error_message" to "Une erreur s'est produite lors du paiement. Veuillez réessayer.",
        "payment.view_booking" to "Voir ma réservation",
        "payment.card_number" to "Numéro de carte",
        "payment.card_holder" to "Nom sur la carte",
        "payment.expiry" to "Expiration",
        "payment.cvv" to "CVV",
        "payment.ssl" to "SSL 256-bit",
        "payment.pci_dss" to "PCI-DSS",
        "payment.3d_secure" to "3D Secure",
        "payment.history" to "Historique des paiements",
        "payment.no_payments" to "Vous n'avez pas encore effectué de paiement.",
        "payment.summary" to "Résumé",
        "payment.total_spent" to "Total dépensé",
        "payment.transactions" to "Transactions",
        "payment.success_rate" to "Taux succès",
        "payment.all_payments" to "Tous",
        "payment.succeeded" to "Réussis",
        "payment.failed" to "Échoués",
        "payment.refunded" to "Remboursés",
        "payment.transaction_id" to "ID Transaction",
        "payment.stripe_reference" to "Référence Stripe",
        "payment.payment_method" to "Méthode",
        "payment.currency" to "Devise",
        "payment.refunded_amount" to "Remboursé",
        
        // Stripe Checkout
        "payment.redirect" to "Redirection",
        "payment.secure_payment" to "Paiement sécurisé",
        "payment.secure_payment_description" to "Vous allez être redirigé vers la page de paiement sécurisée Stripe.",
        "payment.order_summary" to "Récapitulatif de la commande",
        "payment.total" to "Total",
        "payment.loading" to "Chargement...",
        "payment.proceed_to_payment" to "Procéder au paiement",
        "payment.secure_ssl" to "SSL 256-bit",
        "payment.stripe_secure" to "Stripe",
        "payment.pci_compliant" to "PCI-DSS",
        "payment.redirect_to_stripe" to "Page de paiement",
        "payment.redirect_description" to "Complétez votre paiement sur la page Stripe.\nRevenez ici après le paiement.",
        "payment.open_payment_page" to "Ouvrir la page de paiement",
        "payment.check_payment_status" to "Vérifier le paiement",
        "payment.processing_payment" to "Traitement en cours...",
        "payment.processing_description" to "Veuillez patienter pendant que nous vérifions votre paiement.",
        "payment.payment_successful" to "Paiement réussi ! 🎉",
        "payment.payment_success_description" to "Votre réservation est confirmée.\nVous recevrez un email de confirmation.",
        "payment.payment_failed" to "Paiement échoué",
        "payment.payment_failed_description" to "Une erreur s'est produite lors du paiement.\nVeuillez réessayer.",
        "payment.step_summary" to "Résumé",
        "payment.step_payment" to "Paiement",
        "payment.step_confirmation" to "Confirmation",
        
        "components.app_drawer.client" to "Client",
        "components.app_drawer.owner" to "Propriétaire",
        "components.app_drawer.hairstylist" to "Coiffeur",
        "components.app_drawer.admin_user" to "Admin",
        
        // ExternalShareDialog
        "components.external_share_dialog.title" to "Partager vers",
        "components.external_share_dialog.share_via_app" to "Partager via une application",
        "components.external_share_dialog.share_via_app_description" to "Instagram, WhatsApp, Messages, etc.",
        "components.external_share_dialog.error_sharing" to "Erreur lors du partage: {error}",
        "components.external_share_dialog.copy_link" to "Copier le lien",
        "components.external_share_dialog.copy_link_description" to "Copier le lien du post dans le presse-papiers",
        "components.external_share_dialog.error_copying" to "Erreur lors de la copie: {error}",
        "components.external_share_dialog.not_available" to "Le partage externe n'est pas disponible sur cette plateforme",
        
        // SearchTextField
        "components.search_text_field.placeholder" to "Rechercher...",
        "components.search_text_field.content_description" to "Rechercher",
        
        // ========================================
        // ENUMS - LOCALISATION
        // ========================================
        // BookingStatus
        "enums.booking_status.pending" to "En attente",
        "enums.booking_status.confirmed" to "Confirmée",
        "enums.booking_status.in_progress" to "En cours",
        "enums.booking_status.completed" to "Terminée",
        "enums.booking_status.cancelled" to "Annulée",
        "enums.booking_status.no_show" to "Absence",
        
        // PaymentStatus
        "enums.payment_status.pending" to "En attente",
        "enums.payment_status.processing" to "En cours de traitement",
        "enums.payment_status.succeeded" to "Réussi",
        "enums.payment_status.failed" to "Échoué",
        "enums.payment_status.canceled" to "Annulé",
        "enums.payment_status.partially_refunded" to "Partiellement remboursé",
        "enums.payment_status.unpaid" to "Non payé",
        "enums.payment_status.paid" to "Payé",
        "enums.payment_status.refunded" to "Remboursé",
        
        // PostType
        "enums.post_type.general" to "Général",
        "enums.post_type.avant_apres" to "Avant/Après",
        "enums.post_type.portfolio" to "Portfolio",
        "enums.post_type.tendance" to "Tendance",
        "enums.post_type.conseil" to "Conseil",
        "enums.post_type.realisation" to "Réalisation",
        "enums.post_type.inspiration" to "Inspiration",
        "enums.post_type.general_description" to "Post général",
        "enums.post_type.avant_apres_description" to "Montrer une transformation avant/après",
        "enums.post_type.portfolio_description" to "Ajouter à votre portfolio",
        "enums.post_type.tendance_description" to "Partager une tendance coiffure",
        "enums.post_type.conseil_description" to "Donner des conseils et astuces",
        "enums.post_type.realisation_description" to "Montrer une réalisation",
        "enums.post_type.inspiration_description" to "Partager une inspiration",
        
        // PostVisibility
        "enums.post_visibility.public" to "Public",
        "enums.post_visibility.followers" to "Abonnés uniquement",
        "enums.post_visibility.private" to "Privé",
        "enums.post_visibility.public_description" to "Visible par tous",
        "enums.post_visibility.followers_description" to "Visible uniquement par vos abonnés",
        "enums.post_visibility.private_description" to "Visible uniquement par vous",
        
        // ServiceCategory
        "enums.service_category.coupe" to "Coupe & Taille",
        "enums.service_category.coloration" to "Coloration",
        "enums.service_category.soin" to "Soins",
        "enums.service_category.coiffage" to "Coiffage",
        "enums.service_category.barbe" to "Barbier",
        "enums.service_category.technique" to "Techniques Spéciales",
        "enums.service_category.autre" to "Autres Prestations",
        
        // ReactionType
        "enums.reaction_type.like" to "J'aime",
        "enums.reaction_type.love" to "J'adore",
        "enums.reaction_type.wow" to "Wow",
        "enums.reaction_type.inspirant" to "Inspirant",
        "enums.reaction_type.magnifique" to "Magnifique",
        "enums.reaction_type.bravo" to "Bravo",
        "enums.reaction_type.like_description" to "Like classique",
        "enums.reaction_type.love_description" to "J'adore cette couleur !",
        "enums.reaction_type.wow_description" to "Transformation incroyable !",
        "enums.reaction_type.inspirant_description" to "Je veux la même chose !",
        "enums.reaction_type.magnifique_description" to "Travail de qualité !",
        "enums.reaction_type.bravo_description" to "Félicitations au coiffeur !",
        
        // MediaType
        "enums.media_type.before" to "Avant",
        "enums.media_type.after" to "Après",
        "enums.media_type.process" to "Processus",
        "enums.media_type.detail" to "Détail",
        
        // ReportReason
        "enums.report_reason.inapproprie" to "Contenu inapproprié",
        "enums.report_reason.spam" to "Spam publicitaire",
        "enums.report_reason.faux" to "Faux avant/après",
        "enums.report_reason.copyright" to "Violation de droits d'auteur",
        "enums.report_reason.autre" to "Autre",
        "enums.report_reason.inapproprie_description" to "Contenu violent, harcelant ou offensant",
        "enums.report_reason.spam_description" to "Publicité non sollicitée ou contenu répétitif",
        "enums.report_reason.faux_description" to "Transformation ou résultat trompeur",
        "enums.report_reason.copyright_description" to "Utilisation non autorisée de contenu protégé",
        "enums.report_reason.autre_description" to "Autre raison à préciser",
        
        // ReportedEntityType
        "enums.reported_entity_type.post" to "post",
        "enums.reported_entity_type.comment" to "commentaire",
        "enums.reported_entity_type.user" to "utilisateur",
        "enums.reported_entity_type.salon" to "salon",
        
        // VerificationType
        "enums.verification_type.email" to "Email vérifié",
        "enums.verification_type.phone" to "Téléphone vérifié",
        "enums.verification_type.business" to "Entreprise vérifiée",
        "enums.verification_type.professional" to "Professionnel vérifié",
        "enums.verification_type.email_description" to "Email vérifié par confirmation",
        "enums.verification_type.phone_description" to "Numéro de téléphone vérifié",
        "enums.verification_type.business_description" to "Entreprise vérifiée (SIRET, documents)",
        "enums.verification_type.professional_description" to "Diplômes et certifications vérifiés",
        
        // BadgeCategory
        "enums.badge_category.certification" to "Certification",
        "enums.badge_category.competition" to "Compétition",
        "enums.badge_category.formation" to "Formation",
        "enums.badge_category.partenariat" to "Partenariat",
        
        // ReportStatus
        "enums.report_status.pending" to "En attente",
        "enums.report_status.reviewed" to "En cours d'examen",
        "enums.report_status.resolved" to "Résolu",
        "enums.report_status.dismissed" to "Rejeté",
        
        // ModerationAction
        "enums.moderation_action.hide" to "Masquer",
        "enums.moderation_action.delete" to "Supprimer",
        "enums.moderation_action.warn" to "Avertir",
        "enums.moderation_action.hide_description" to "Le contenu sera masqué pour tous les utilisateurs sauf l'auteur et les administrateurs.",
        "enums.moderation_action.delete_description" to "Le contenu sera supprimé définitivement et ne pourra pas être restauré.",
        "enums.moderation_action.warn_description" to "Un avertissement sera envoyé à l'auteur sans modifier le contenu.",

        // AppealStatus
        "enums.appeal_status.none" to "Aucun appel",
        "enums.appeal_status.pending" to "En attente",
        "enums.appeal_status.approved" to "Approuvé",
        "enums.appeal_status.rejected" to "Rejeté",

        // ModerationAppealStatus
        "enums.appeal_status.none" to "Aucun appel",
        "enums.appeal_status.pending" to "En attente",
        "enums.appeal_status.approved" to "Approuvé",
        "enums.appeal_status.rejected" to "Rejeté",
        
        // HairHashtagCategory
        "enums.hair_hashtag_category.technique" to "Technique",
        "enums.hair_hashtag_category.style" to "Style",
        "enums.hair_hashtag_category.couleur" to "Couleur",
        "enums.hair_hashtag_category.longueur" to "Longueur",
        "enums.hair_hashtag_category.texture" to "Texture",
        
        // FollowingType
        "enums.following_type.coiffeur" to "Coiffeur",
        "enums.following_type.salon" to "Salon",
    )
    
    return StringsBundle(strings)
}

