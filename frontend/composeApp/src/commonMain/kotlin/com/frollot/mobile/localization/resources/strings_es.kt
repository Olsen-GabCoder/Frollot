package com.frollot.mobile.localization.resources

/**
 * Bundle de strings españolas.
 * 
 * Traducciones completas para todas las pantallas y componentes de la aplicación.
 */
fun createSpanishStrings(): StringsBundle {
    val strings = mapOf<String, String>(
        // ========================================
        // PANTALLA DE INICIO DE SESIÓN
        // ========================================
        "login.welcome_title" to "Bienvenido 👋",
        "login.welcome_subtitle" to "Inicia sesión y explora\nposibilidades infinitas",
        "login.email_label" to "Dirección de correo electrónico",
        "login.email_placeholder" to "tu@ejemplo.com",
        "login.password_label" to "Contraseña",
        "login.password_placeholder" to "••••••••",
        "login.forgot_password" to "¿Olvidaste tu contraseña?",
        "login.submit_button" to "Iniciar sesión",
        "login.submit_button_loading" to "Iniciando sesión...",
        "login.continue_with" to "O CONTINÚA CON",
        "login.google_button" to "Google",
        "login.facebook_button" to "Facebook",
        "login.new_here" to "¿Nuevo aquí?",
        "login.create_account" to "Crear una cuenta",
        "login.secure_connection" to "Conexión 100% segura",
        
        // Mensajes de error de inicio de sesión
        "login.errors.invalid_credentials" to "Correo electrónico o contraseña incorrectos",
        "login.errors.account_disabled" to "Cuenta desactivada. Por favor, contacta con soporte.",
        "login.errors.account_not_found" to "Cuenta no encontrada",
        "login.errors.server_unavailable" to "No se puede contactar con el servidor",
        "login.errors.timeout" to "Tiempo de espera agotado. Verifica tu conexión a internet.",
        "login.errors.generic_error" to "Error al iniciar sesión",
        
        // ========================================
        // PANTALLA DE REGISTRO
        // ========================================
        "register.welcome_title" to "Únete a nosotros 🎉",
        "register.welcome_subtitle" to "Crea tu cuenta en segundos",
        "register.first_name_label" to "Nombre",
        "register.first_name_placeholder" to "Sofía",
        "register.last_name_label" to "Apellido",
        "register.last_name_placeholder" to "Martín",
        "register.email_label" to "Dirección de correo electrónico",
        "register.email_placeholder" to "tu@ejemplo.com",
        "register.account_type_label" to "Tipo de cuenta",
        "register.password_label" to "Contraseña",
        "register.password_placeholder" to "Mínimo 8 caracteres",
        "register.confirm_password_label" to "Confirmar contraseña",
        "register.confirm_password_placeholder" to "Vuelve a escribir tu contraseña",
        "register.password_mismatch" to "Las contraseñas no coinciden",
        "register.submit_button" to "Crear mi cuenta",
        "register.submit_button_loading" to "Creando cuenta...",
        "register.sign_up_with" to "O REGÍSTRATE CON",
        "register.google_button" to "Google",
        "register.facebook_button" to "Facebook",
        "register.already_registered" to "¿Ya estás registrado?",
        "register.login_link" to "Iniciar sesión",
        "register.data_protected" to "Tus datos están protegidos",
        "register.success_message" to "¡Bienvenido {firstName} {lastName}! Tu cuenta ha sido creada con éxito.",
        
        // Tipos de cuenta
        "register.user_types.client" to "👤 Cliente",
        "register.user_types.hairstylist" to "✂️ Peluquero",
        "register.user_types.salon_owner" to "🏢 Propietario de salón",
        "register.user_types.admin" to "⚙️ Administrador",
        
        // Mensajes de error de registro
        "register.errors.email_already_used" to "Este correo electrónico ya está en uso",
        "register.errors.invalid_data" to "Datos inválidos, verifica tu información",
        "register.errors.server_unavailable" to "No se puede contactar con el servidor",
        "register.errors.timeout" to "Tiempo de espera agotado. Verifica tu conexión a internet.",
        "register.errors.generic_error" to "Error al registrarse",
        
        // ========================================
        // PANTALLA DE INICIO
        // ========================================
        "home.title" to "Frollot",
        "home.filters_button" to "Filtros",
        "home.my_bookings_button" to "Mis reservas",
        "home.create_salon_button" to "Crear un salón",
        "home.loading_message" to "Cargando salones",
        "home.error_message" to "No se pueden cargar los salones",
        "home.empty_state_title" to "No hay salones disponibles",
        "home.empty_state_title_with_filters" to "Ningún salón coincide con tus criterios",
        "home.empty_state_message" to "¡Sé el primero en crear un salón!",
        "home.empty_state_message_with_filters" to "Intenta ampliar tu búsqueda o quitar algunos filtros.",
        "home.create_my_salon" to "Crear mi salón",
        "home.premium_salons" to "Salones Premium",
        "home.near_me" to "Cerca de mí",
        "home.city" to "Ciudad",
        "home.salons_found" to "{count} salón encontrado",
        "home.salons_found_plural" to "{count} salones encontrados",
        "home.filter_by_city" to "Filtrar por ciudad",
        "home.filter_by_city_description" to "Ingresa una ciudad para filtrar los salones.",
        "home.city_placeholder" to "Ej: Madrid",
        "home.apply" to "Aplicar",
        "home.reset" to "Restablecer",
        "home.premium_badge" to "PREMIUM",
        "home.rating" to "Calificación",
        "home.open_hours" to "Abierto • 9h-19h",
        "home.cut" to "Corte",
        "home.coloring" to "Coloración",
        "home.book" to "Reservar",
        "home.cover_photo_description" to "Foto de portada de {salonName}",
        
        // ========================================
        // PANTALLA DE CONFIGURACIÓN
        // ========================================
        "settings.title" to "Configuración",
        
        // Secciones
        "settings.sections.account" to "Cuenta",
        "settings.sections.privacy" to "Privacidad",
        "settings.sections.notifications" to "Notificaciones",
        "settings.sections.appearance" to "Apariencia",
        "settings.sections.social_network" to "Red social",
        "settings.sections.bookings" to "Reservas",
        "settings.sections.content_and_media" to "Contenido y medios",
        "settings.sections.data_privacy" to "Privacidad de datos",
        "settings.sections.help_and_support" to "Ayuda y soporte",
        "settings.sections.about" to "Acerca de",
        
        // Cuenta
        "settings.account.profile" to "Perfil",
        "settings.account.profile_subtitle" to "Gestiona tu perfil e información",
        "settings.account.security" to "Seguridad",
        "settings.account.security_subtitle" to "Contraseña, autenticación de dos factores",
        "settings.account.email" to "Correo electrónico",
        "settings.account.phone" to "Teléfono",
        "settings.account.phone_subtitle" to "Agregar un número de teléfono",
        "settings.account.not_defined" to "No definido",
        
        // Privacidad
        "settings.privacy.profile_visibility" to "Visibilidad del perfil",
        "settings.privacy.who_can_follow_me" to "Quién puede seguirme",
        "settings.privacy.who_can_message_me" to "Quién puede enviarme mensajes",
        "settings.privacy.show_activity_status" to "Mostrar estado de actividad",
        "settings.privacy.show_activity_status_subtitle" to "Otros usuarios verán cuando estés en línea",
        "settings.privacy.blocked_users" to "Usuarios bloqueados",
        "settings.privacy.blocked_users_count" to "{count} usuario(s)",
        "settings.privacy.no_blocked_users" to "No hay usuarios bloqueados",
        "settings.privacy.options.public" to "Público",
        "settings.privacy.options.followers_only" to "Solo seguidores",
        "settings.privacy.options.private" to "Privado",
        "settings.privacy.options.everyone" to "Todos",
        "settings.privacy.options.nobody" to "Nadie",
        
        // Notificaciones
        "settings.notifications.title" to "Notificaciones",
        "settings.notifications.subtitle" to "Activar o desactivar todas las notificaciones",
        "settings.notifications.push" to "Notificaciones push",
        "settings.notifications.push_subtitle" to "Recibir notificaciones en tu dispositivo",
        "settings.notifications.email" to "Notificaciones por correo electrónico",
        "settings.notifications.email_subtitle" to "Recibir notificaciones por correo electrónico",
        "settings.notifications.bookings" to "Reservas",
        "settings.notifications.bookings_subtitle" to "Notificaciones para tus citas",
        "settings.notifications.social" to "Red social",
        "settings.notifications.social_subtitle" to "Me gusta, comentarios, menciones, etc.",
        "settings.notifications.marketing" to "Marketing",
        "settings.notifications.marketing_subtitle" to "Ofertas especiales y novedades",
        
        // Apariencia
        "settings.appearance.dark_mode" to "Modo oscuro",
        "settings.appearance.dark_mode_subtitle" to "Activar el tema oscuro",
        "settings.appearance.language" to "Idioma",
        
        // Red social
        "settings.social_network.post_visibility_default" to "Visibilidad predeterminada de las publicaciones",
        "settings.social_network.allow_comments" to "Permitir comentarios",
        "settings.social_network.allow_comments_subtitle" to "Otros usuarios pueden comentar tus publicaciones",
        "settings.social_network.allow_reactions" to "Permitir reacciones",
        "settings.social_network.allow_reactions_subtitle" to "Otros usuarios pueden reaccionar a tus publicaciones",
        "settings.social_network.allow_shares" to "Permitir compartir",
        "settings.social_network.allow_shares_subtitle" to "Otros usuarios pueden compartir tus publicaciones",
        
        // Reservas
        "settings.bookings.booking_notifications" to "Notificaciones de citas",
        "settings.bookings.booking_notifications_subtitle" to "Recibir notificaciones para tus citas",
        "settings.bookings.availability_preferences" to "Preferencias de disponibilidad",
        "settings.bookings.availability_preferences_subtitle" to "Gestiona tus horarios preferidos",
        "settings.bookings.payment_methods" to "Métodos de pago",
        "settings.bookings.payment_methods_subtitle" to "Gestiona tus métodos de pago",
        
        // Contenido y medios
        "settings.content_and_media.auto_save_photos" to "Guardar fotos automáticamente",
        "settings.content_and_media.auto_save_photos_subtitle" to "Guardar fotos en tu galería",
        "settings.content_and_media.data_usage" to "Uso de datos",
        "settings.content_and_media.video_quality" to "Calidad de video",
        "settings.content_and_media.data_usage_options.economical" to "Económico",
        "settings.content_and_media.data_usage_options.standard" to "Estándar",
        "settings.content_and_media.data_usage_options.high" to "Alto",
        "settings.content_and_media.video_quality_options.sd" to "SD",
        "settings.content_and_media.video_quality_options.hd" to "HD",
        "settings.content_and_media.video_quality_options.full_hd" to "Full HD",
        
        // Privacidad de datos
        "settings.data_privacy.download_data" to "Descargar tus datos",
        "settings.data_privacy.download_data_subtitle" to "Obtener una copia de tus datos",
        "settings.data_privacy.delete_account" to "Eliminar tu cuenta",
        "settings.data_privacy.delete_account_subtitle" to "Eliminar permanentemente tu cuenta y todos tus datos",
        
        // Ayuda y soporte
        "settings.help_and_support.help_center" to "Centro de ayuda",
        "settings.help_and_support.help_center_subtitle" to "Preguntas frecuentes y guías de uso",
        "settings.help_and_support.contact_us" to "Contáctanos",
        "settings.help_and_support.contact_us_subtitle" to "Reportar un problema o hacer una pregunta",
        "settings.help_and_support.report_bug" to "Reportar un error",
        "settings.help_and_support.report_bug_subtitle" to "Ayúdanos a mejorar la aplicación",
        "settings.help_and_support.rate_app" to "Calificar la aplicación",
        "settings.help_and_support.rate_app_subtitle" to "Comparte tu opinión en la tienda",
        
        // Acerca de
        "settings.about.version" to "Versión",
        "settings.about.terms_of_service" to "Términos de servicio",
        "settings.about.terms_of_service_subtitle" to "Leer los términos de servicio",
        "settings.about.privacy_policy" to "Política de privacidad",
        "settings.about.privacy_policy_subtitle" to "Leer la política de privacidad",
        "settings.about.licenses" to "Licencias",
        "settings.about.licenses_subtitle" to "Licencias de código abierto",
        
        // Acciones
        "settings.actions.logout" to "Cerrar sesión",
        "settings.actions.logout_dialog_title" to "Cerrar sesión",
        "settings.actions.logout_dialog_text" to "¿Estás seguro de que quieres cerrar sesión?",
        "settings.actions.delete" to "Eliminar",
        "settings.actions.delete_account_dialog_title" to "Eliminar tu cuenta",
        "settings.actions.delete_account_dialog_text" to "Esta acción es irreversible. Todos tus datos serán eliminados permanentemente. ¿Estás absolutamente seguro?",
        "settings.actions.cancel" to "Cancelar",
        
        // ========================================
        // PANTALLA DE PERFIL
        // ========================================
        "profile.version" to "Versión 1.0.0",
        "profile.logout_dialog_title" to "Cerrar sesión",
        "profile.logout_dialog_text" to "¿Estás seguro de que quieres cerrar sesión?",
        "profile.logout_confirm" to "Sí, cerrar sesión",
        "profile.cancel" to "Cancelar",
        "profile.avatar_preview" to "Vista previa del avatar",
        "profile.save" to "Guardar",
        "profile.edit_photo" to "Editar foto",
        "profile.account_verified" to "Cuenta verificada",
        "profile.account_not_verified" to "No verificado",
        "profile.account_info" to "Información de la cuenta",
        "profile.email" to "Correo electrónico",
        "profile.phone" to "Teléfono",
        "profile.member_since" to "Miembro desde",
        "profile.account_type" to "Tipo de cuenta",
        "profile.edit_profile" to "Editar mi perfil",
        "profile.edit_profile_subtitle" to "Cambiar nombre, apellido, teléfono",
        "profile.change_password" to "Cambiar contraseña",
        "profile.change_password_subtitle" to "Actualizar tus credenciales",
        "profile.logout" to "Cerrar sesión",
        "profile.logout_subtitle" to "Terminar tu sesión",
        "profile.statistics" to "Estadísticas",
        "profile.salons" to "Salones",
        "profile.bookings" to "Reservas",
        "profile.reviews" to "Reseñas",
        "profile.services" to "Servicios",
        "profile.points" to "Puntos",
        "profile.user_types.client" to "Cliente",
        "profile.user_types.salon_owner" to "Propietario de salón",
        "profile.user_types.hairstylist" to "Peluquero",
        "profile.user_types.admin" to "Administrador",
        
        // ========================================
        // PANTALLA DE FEED SOCIAL
        // ========================================
        "social_feed.filter_by_type" to "Filtrar por tipo",
        "social_feed.my_follows" to "Mis seguimientos",
        "social_feed.near_me" to "Cerca de mí",
        "social_feed.all" to "Todos",
        "social_feed.add_comment" to "Agregar un comentario (opcional)",
        "social_feed.share" to "Compartir",
        "social_feed.cancel" to "Cancelar",
        "social_feed.no_collection_yet" to "Aún no tienes ninguna colección",
        "social_feed.save" to "Guardar",
        "social_feed.archive" to "Archivar",
        "social_feed.share_in_app" to "Compartir en la app",
        "social_feed.share_external" to "Compartir externamente",
        "social_feed.report" to "Reportar",
        "social_feed.add_to_collection_example" to "Ej.: ¡Reservé en este salón gracias a esta publicación!",
        "social_feed.add_to_collection" to "Añadir a colección",
        "social_feed.create_collection" to "Crear colección",
        
        // ========================================
        // PANTALLA MIS RESERVAS
        // ========================================
        "my_bookings.service" to "Servicio",
        "my_bookings.date_time" to "Fecha y Hora",
        "my_bookings.hairstylist" to "Peluquero",
        "my_bookings.amount" to "Importe",
        "my_bookings.review_left" to "Reseña dejada",
        "my_bookings.leave_review" to "Dejar una reseña",
        "my_bookings.cancel" to "Cancelar",
        "my_bookings.cancel_confirm" to "Sí, cancelar",
        "my_bookings.cancel_keep" to "No, mantener",
        
        // ========================================
        // PANTALLA DE RESERVA (ASISTENTE)
        // ========================================
        "booking.back_to_home" to "Volver al inicio",
        "booking.duration" to "Duración",
        "booking.category" to "Categoría",
        "booking.choose_expert" to "Elige tu experto",
        "booking.choose_expert_subtitle" to "Selecciona un peluquero o deja que el salón elija",
        "booking.select_date" to "Selecciona una fecha",
        "booking.select_date_subtitle" to "Elige el día de tu cita",
        "booking.choose_time_slot" to "Elige un horario",
        "booking.choose_time_slot_subtitle" to "Selecciona la hora que te convenga",
        "booking.notes_or_special_requests" to "Notas o solicitudes especiales",
        "booking.notes_or_special_requests_subtitle" to "Opcional - Infórmanos de tus preferencias",
        "booking.notes_info" to "Esta información se transmitirá a tu peluquero",
        "booking.expert_selected" to "Experto seleccionado",
        "booking.continue" to "Continuar",
        "booking.time_slot_selected" to "Horario seleccionado",
        "booking.pay_now" to "Pagar ahora",
        "booking.view_my_bookings" to "Ver mis reservas",
        
        // ========================================
        // PANTALLA DE DETALLE DE RESERVA
        // ========================================
        "booking_detail.cancel_keep" to "No, mantener",
        "booking_detail.hair_salon" to "Salón de Peluquería",
        "booking_detail.click_for_details" to "Haz clic para ver los detalles",
        "booking_detail.service" to "Servicio",
        "booking_detail.service_details" to "Detalles de tu servicio",
        "booking_detail.booking" to "Reserva",
        "booking_detail.booking_details" to "Fecha, hora y peluquero",
        "booking_detail.payment" to "Pago",
        "booking_detail.payment_details" to "Detalles de la transacción",
        "booking_detail.amount" to "IMPORTE",
        "booking_detail.cancel_booking_title" to "¿Cancelar reserva?",
        "booking_detail.cancel_confirm_message" to "¿Estás seguro de que quieres cancelar esta reserva? Esta acción es irreversible.",
        "booking_detail.booking_status" to "Estado de tu reserva",
        
        // ========================================
        // PANTALLA DE DETALLE DE SALÓN
        // ========================================
        "salon_detail.services" to "Servicios",
        "salon_detail.view_posts" to "Ver publicaciones",
        "salon_detail.open_feed" to "Abrir feed",
        "salon_detail.add_member" to "Agregar un miembro",
        "salon_detail.queue" to "Cola",
        "salon_detail.position" to "Posición: #{position}",
        "salon_detail.leave_queue" to "Salir de la cola",
        "salon_detail.join_queue" to "Unirse a la cola",
        "salon_detail.login_to_join_queue" to "Inicia sesión para unirte a la cola",
        "salon_detail.cancel" to "Cancelar",
        "salon_detail.subscribers" to "Suscriptores",
        "salon_detail.book" to "Reservar",
        "salon_detail.salon_verified" to "Salón verificado",
        "salon_detail.clients" to "Clientes",
        "salon_detail.waiting" to "Esperando",
        "salon_detail.your_position" to "Tu posición",
        "salon_detail.position_in_queue" to "Número {position} en la cola",
        "salon_detail.my_team" to "Mi Equipo",
        "salon_detail.team" to "Equipo",
        "salon_detail.our_services" to "Nuestros Servicios",
        "salon_detail.choose_from_services" to "Elige entre {count} servicio",
        "salon_detail.choose_from_services_plural" to "Elige entre {count} servicios",
        "salon_detail.loading_services" to "Cargando servicios",
        "salon_detail.please_wait" to "Por favor espera...",
        "salon_detail.loading_error" to "Error de carga",
        "salon_detail.no_services_available" to "No hay servicios disponibles",
        "salon_detail.no_services_message" to "Este salón aún no ha agregado servicios.\n¡Vuelve más tarde!",
        "salon_detail.ready_to_book" to "¿Listo para reservar?",
        "salon_detail.reviews_and_ratings" to "Reseñas y Calificaciones",
        
        // ========================================
        // PANTALLA DE CREACIÓN DE SALÓN
        // ========================================
        "create_salon.launch_your_salon" to "Lanza tu salón",
        "create_salon.join_community" to "Únete a nuestra comunidad de profesionales",
        "create_salon.salon_info" to "Información del salón",
        "create_salon.cover_photo" to "Foto de portada",
        "create_salon.cover_photo_hint" to "Opcional • JPG, PNG • Máx. 10MB",
        "create_salon.add_photo" to "Agregar una foto",
        "create_salon.click_to_browse" to "Haz clic para explorar tus archivos",
        "create_salon.data_secured" to "Tus datos están seguros y protegidos",
        
        // ========================================
        // PANTALLA DE DETALLE DE PUBLICACIÓN
        // ========================================
        "post_detail.comments" to "{count} comentario",
        "post_detail.comments_plural" to "{count} comentarios",
        
        // ========================================
        // PANTALLA DE CREACIÓN DE PUBLICACIÓN
        // ========================================
        "create_post.public" to "Público",
        "create_post.post_type" to "Tipo de publicación",
        "create_post.visibility" to "Visibilidad",
        "create_post.at_least_two_images" to "Al menos 2 imágenes (Antes + Después)",
        "create_post.click_to_select_photo" to "Haz clic para seleccionar una foto",
        "create_post.photo_format_hint" to "JPG, PNG (máx. 10MB)",
        "create_post.post_will_be_visible" to "Tu publicación será visible para todos los usuarios",
        
        // ========================================
        // PANTALLA DE PERFIL DE PELUQUERO
        // ========================================
        "coiffeur_profile.pinned_posts" to "Publicaciones fijadas",
        "coiffeur_profile.recent_posts" to "Publicaciones recientes",
        "coiffeur_profile.unfollow" to "Dejar de seguir",
        "coiffeur_profile.follow" to "Seguir",
        "coiffeur_profile.badges_and_certifications" to "Insignias y Certificaciones",
        "coiffeur_profile.featured_portfolio" to "Portafolio destacado",
        "coiffeur_profile.portfolios" to "Portafolios",
        
        // ========================================
        // PANTALLA DE PERFIL SOCIAL DE SALÓN
        // ========================================
        "salon_social_profile.recent_posts" to "Publicaciones recientes",
        "salon_social_profile.unfollow" to "Dejar de seguir",
        "salon_social_profile.follow" to "Seguir",
        "salon_social_profile.featured_posts" to "Publicaciones destacadas",
        "salon_social_profile.portfolios" to "Portafolios",
        "salon_social_profile.verified" to "Verificado",
        "salon_social_profile.services" to "Servicios",
        
        // ========================================
        // PANTALLA DE PERFIL DE CLIENTE
        // ========================================
        "client_profile.title" to "Perfil de Cliente",
        "client_profile.about" to "Acerca de",
        "client_profile.posts" to "Posts",
        "client_profile.likes" to "Me gusta",
        "client_profile.followers" to "Seguidores",
        "client_profile.following" to "Siguiendo",
        "client_profile.collections" to "Colecciones",
        "client_profile.collections_count" to "Colecciones",
        "client_profile.recent_posts" to "Posts recientes",
        "client_profile.badges" to "Insignias",
        "client_profile.follow" to "Seguir",
        "client_profile.unfollow" to "Dejar de seguir",
        
        // ========================================
        // PANTALLA DE PERFIL DE PROPIETARIO DE SALÓN
        // ========================================
        "salon_owner_profile.title" to "Perfil de Propietario",
        "salon_owner_profile.about" to "Acerca de",
        "salon_owner_profile.posts" to "Posts",
        "salon_owner_profile.likes" to "Me gusta",
        "salon_owner_profile.followers" to "Seguidores",
        "salon_owner_profile.salons" to "Salones",
        "salon_owner_profile.collections" to "Colecciones",
        "salon_owner_profile.collections_count" to "Colecciones",
        "salon_owner_profile.recent_posts" to "Posts recientes",
        "salon_owner_profile.badges" to "Insignias",
        "salon_owner_profile.follow" to "Seguir",
        "salon_owner_profile.unfollow" to "Dejar de seguir",
        
        // ========================================
        // PANTALLA DE COMENTARIOS
        // ========================================
        "comments.title" to "Comentarios",
        "comments.add_comment" to "Añadir un comentario...",
        "comments.load_more" to "Cargar más comentarios",
        
        // ========================================
        // PANTALLA DE COLECCIONES
        // ========================================
        "collections.title" to "Colecciones",
        "collections.post" to "publicación",
        "collections.posts" to "publicaciones",
        "collections.edit" to "Editar",
        "collections.delete" to "Eliminar",
        "collections.new_collection" to "Nueva colección",
        "collections.name" to "Nombre *",
        "collections.description" to "Descripción (opcional)",
        "collections.category" to "Categoría",
        "collections.public_collection" to "Colección pública",
        "collections.save_to_collection" to "Guardar en colección",
        "collections.no_collections" to "Ninguna colección. ¡Crea una primero!",
        
        // ========================================
        // PANTALLA DE DETALLE DE COLECCIÓN
        // ========================================
        "collection_detail.title" to "Colección",
        "collection_detail.load_more" to "Cargar más",
        "collection_detail.edit" to "Editar",
        "collection_detail.delete" to "Eliminar",
        "collection_detail.delete_collection" to "Eliminar colección",
        "collection_detail.delete_collection_message" to "¿Estás seguro de que quieres eliminar esta colección? Todos los posts serán removidos.",
        "collection_detail.remove_from_collection" to "Quitar de la colección",
        
        // ========================================
        // PANTALLA DE CREACIÓN DE PORTAFOLIO
        // ========================================
        "create_portfolio.title" to "Crear un portafolio",
        "create_portfolio.no_salon_found" to "No se encontró ningún salón. Por favor, crea un salón primero.",
        "create_portfolio.salon" to "Salón",
        "create_portfolio.cover_image" to "Imagen de portada",
        "create_portfolio.add_cover_image" to "Agregar una imagen de portada",
        "create_portfolio.public_portfolio" to "Portafolio público",
        "create_portfolio.name" to "Nombre del portfolio *",
        "create_portfolio.name_placeholder" to "Ej: Mis Coloraciones 2024",
        "create_portfolio.description" to "Descripción",
        
        // ========================================
        // PANTALLA DE DETALLE DE PORTAFOLIO
        // ========================================
        "portfolio_detail.load_more" to "Cargar más",
        
        // ========================================
        // PANTALLA DE LISTA DE PORTAFOLIOS
        // ========================================
        "portfolios_list.create_portfolio" to "Crear un portafolio",
        
        // ========================================
        // PANTALLA DE CREACIÓN DE SERVICIO
        // ========================================
        "create_service.premium_service" to "Servicio premium",
        "create_service.service_info" to "Información del servicio",
        "create_service.category" to "Categoría *",
        "create_service.price_adjustment_info" to "Los precios se pueden ajustar en cualquier momento desde tu espacio profesional.",
        "create_service.name" to "Nombre del servicio *",
        "create_service.name_placeholder" to "Ej: Corte Hombre, Coloración completa...",
        "create_service.description" to "Descripción",
        "create_service.duration" to "Duración (min) *",
        "create_service.price" to "Precio (€) *",
        
        // ========================================
        // PANTALLA DE PUBLICACIONES DE SALÓN
        // ========================================
        "salon_posts.title" to "Publicaciones de {salonName}",
        "salon_posts.all" to "Todos",
        "salon_posts.all_services" to "Todos los servicios",
        "salon_posts.popular" to "Populares",
        
        // ========================================
        // PANTALLA DE ARCHIVOS
        // ========================================
        "archives.title" to "Mis Archivos",
        
        // ========================================
        // PANTALLA DE FAVORITOS
        // ========================================
        "favorites.title" to "Mis Favoritos",
        "favorites.offline_mode" to "Modo sin conexión - Datos en caché",
        
        // ========================================
        // PANTALLA DE TENDENCIAS
        // ========================================
        "trending.title" to "Tendencias",
        "trending.posts" to "Publicaciones",
        "trending.hashtags" to "Hashtags",
        "trending.salons" to "Salones",
        
        // ========================================
        // PANTALLA DE REPORTE
        // ========================================
        "report.info_message" to "Tu reporte será revisado por nuestro equipo de moderación. Gracias por ayudarnos a mantener una comunidad respetuosa.",
        "report.reason_title" to "Motivo del reporte *",
        "report.additional_info" to "Información adicional (opcional)",
        "report.cancel" to "Cancelar",
        "report.submit" to "Reportar",
        "report.reported_content" to "Contenido reportado",
        "report.post_author" to "Autor de la publicación",
        
        // ========================================
        // PANTALLA DE CREACIÓN DE PERSONAL
        // ========================================
        "create_staff.title" to "Agregar un miembro",
        "create_staff.new_collaborator" to "Nuevo colaborador",
        "create_staff.personal_info" to "Información personal",
        "create_staff.specialties" to "Especialidades",
        "create_staff.specialties_hint" to "Selecciona las categorías de servicios que este miembro puede realizar.",
        "create_staff.email_info" to "El colaborador recibirá un correo electrónico con su información de inicio de sesión.",
        "create_staff.first_name" to "Nombre *",
        "create_staff.first_name_placeholder" to "Ej: Juan",
        "create_staff.last_name" to "Apellido *",
        "create_staff.last_name_placeholder" to "Ej: García",
        "create_staff.email" to "Email *",
        
        // ========================================
        // PANTALLA DE GESTIÓN DE COLA
        // ========================================
        "queue_management.title" to "{salonName} - Gestión de Cola",
        "queue_management.ticket" to "Ticket #{position}",
        "queue_management.arrived" to "Llegado",
        "queue_management.estimated_wait" to "Tiempo de espera estimado",
        "queue_management.call_next" to "Llamar siguiente",
        "queue_management.remove" to "Eliminar",
        
        // ========================================
        // PANTALLA CAMBIAR EMAIL
        // ========================================
        "change_email.title" to "Cambiar email",
        "change_email.current_email" to "Email actual",
        "change_email.new_email_section" to "Nuevo email",
        "change_email.new_email" to "Nuevo email",
        "change_email.confirm_with_password" to "Confirmar con tu contraseña",
        "change_email.save" to "Guardar",
        "change_email.saving" to "Guardando...",
        "change_email.all_fields_required" to "Por favor, rellena todos los campos",
        "change_email.invalid_email" to "Dirección de email inválida",
        "change_email.error" to "Error al cambiar el email",
        "change_email.info" to "Tu nuevo email se usará para iniciar sesión en tu cuenta.",
        
        // ========================================
        // PANTALLA CAMBIAR TELÉFONO
        // ========================================
        "change_phone.title" to "Cambiar teléfono",
        "change_phone.current_phone" to "Teléfono actual",
        "change_phone.not_defined" to "No definido",
        "change_phone.new_phone_section" to "Nuevo número",
        "change_phone.new_phone" to "Nuevo número de teléfono",
        "change_phone.phone_placeholder" to "+34 612 345 678",
        "change_phone.leave_blank_to_remove" to "Dejar vacío para eliminar",
        "change_phone.confirm_with_password" to "Confirmar con tu contraseña",
        "change_phone.save" to "Guardar",
        "change_phone.saving" to "Guardando...",
        "change_phone.password_required" to "La contraseña es obligatoria",
        "change_phone.error" to "Error al cambiar el teléfono",
        
        // ========================================
        // PANTALLA DE SEGURIDAD
        // ========================================
        "security.title" to "Seguridad",
        "security.change_password" to "Cambiar contraseña",
        "security.current_password" to "Contraseña actual",
        "security.new_password" to "Nueva contraseña",
        "security.confirm_password" to "Confirmar contraseña",
        "security.password_requirements" to "Al menos 8 caracteres",
        "security.change_password_button" to "Cambiar contraseña",
        "security.changing" to "Cambiando...",
        "security.all_fields_required" to "Todos los campos son obligatorios",
        "security.passwords_do_not_match" to "Las contraseñas no coinciden",
        "security.password_too_short" to "La contraseña debe tener al menos 8 caracteres",
        "security.change_password_error" to "Error al cambiar la contraseña",
        "security.password_changed" to "Contraseña cambiada con éxito",
        "security.active_sessions" to "Sesiones activas",
        "security.no_active_sessions" to "No hay sesiones activas",
        "security.current_session" to "Esta sesión",
        "security.other_session" to "Otro dispositivo",
        "security.current_badge" to "Actual",
        "security.created_at" to "Creada el",
        "security.expires_at" to "Expira el",
        "security.revoke" to "Revocar",
        "security.revoke_all" to "Revocar todo",
        "security.revoke_session_title" to "Revocar sesión",
        "security.revoke_session_message" to "¿Estás seguro de que deseas desconectar este dispositivo?",
        "security.revoke_all_sessions" to "Desconectar todos los dispositivos",
        "security.revoke_all_sessions_title" to "Desconectar todos los dispositivos",
        "security.revoke_all_sessions_message" to "¿Estás seguro de que deseas desconectar todos los demás dispositivos? Tendrás que iniciar sesión de nuevo en cada uno.",
        "security.session_revoked" to "Sesión revocada con éxito",
        "security.all_sessions_revoked" to "Todas las otras sesiones han sido revocadas",
        "security.revoke_session_error" to "Error al revocar la sesión",
        "security.revoke_all_sessions_error" to "Error al revocar las sesiones",
        "security.security_tip" to "Consejo: Cambia tu contraseña regularmente y desconecta los dispositivos que no reconozcas.",
        
        // Indicador de fuerza de contraseña
        "security.password_strength" to "Fuerza de la contraseña",
        "security.password_weak" to "Débil",
        "security.password_fair" to "Regular",
        "security.password_good" to "Buena",
        "security.password_strong" to "Fuerte",
        
        // Estado de seguridad
        "security.security_status" to "Estado de seguridad",
        "security.account_protected" to "Tu cuenta está protegida",
        "security.active_devices" to "Dispositivos activos",
        "security.password_set" to "Contraseña establecida",
        
        // Actualizaciones en tiempo real
        "security.real_time_updates" to "Actualización en tiempo real",
        
        // Consejos de seguridad mejorados
        "security.security_tips_title" to "Consejos de seguridad",
        "security.tip_1" to "Usa una contraseña única de al menos 12 caracteres con mayúsculas, minúsculas, números y símbolos.",
        "security.tip_2" to "Nunca compartas tu contraseña y activa la autenticación de dos factores si está disponible.",
        "security.tip_3" to "Revisa regularmente tus sesiones activas y desconecta cualquier dispositivo sospechoso.",
        
        // ========================================
        // ELIMINAR CUENTA
        // ========================================
        "delete_account.title" to "Eliminar cuenta",
        "delete_account.warning" to "Esta acción es irreversible. Todos tus datos serán eliminados permanentemente.",
        "delete_account.confirm_title" to "¿Eliminar tu cuenta?",
        "delete_account.confirm_message" to "Esta acción eliminará permanentemente tu cuenta y todos tus datos (reservas, reseñas, fotos, etc.). Esta acción es irreversible.",
        "delete_account.password_label" to "Ingresa tu contraseña para confirmar",
        "delete_account.confirm_checkbox" to "Entiendo que esta acción es irreversible",
        "delete_account.delete_button" to "Eliminar mi cuenta",
        "delete_account.deleting" to "Eliminando...",
        "delete_account.success" to "Tu cuenta ha sido eliminada con éxito",
        "delete_account.error" to "Error al eliminar la cuenta",
        
        // ========================================
        // PANTALLA MÉTODOS DE PAGO
        // ========================================
        "payment_methods.title" to "Métodos de pago",
        "payment_methods.no_cards" to "No hay tarjetas guardadas",
        "payment_methods.description" to "Tus tarjetas de pago se guardarán de forma segura durante tu próxima compra.",
        "payment_methods.stripe_info" to "Tu información de pago está protegida por Stripe. Nunca almacenamos los números de tus tarjetas.",
        "payment_methods.add_card" to "Añadir tarjeta",
        
        // ========================================
        // PANTALLA USUARIOS BLOQUEADOS
        // ========================================
        "blocked_users.title" to "Usuarios bloqueados",
        "blocked_users.no_blocked_users" to "No hay usuarios bloqueados",
        "blocked_users.description" to "Los usuarios que bloquees no podrán ver tu perfil ni contactarte.",
        "blocked_users.unblock" to "Desbloquear",
        
        // ========================================
        // CENTRO DE AYUDA
        // ========================================
        "help_center.title" to "Centro de ayuda",
        "help_center.faq_title" to "Preguntas frecuentes",
        "help_center.need_more_help" to "¿No encontraste una respuesta?",
        "help_center.contact_support" to "Contactar soporte",
        "help_center.faq1_question" to "¿Cómo cancelo una reserva?",
        "help_center.faq1_answer" to "Ve a 'Mis reservas', selecciona la reserva y pulsa 'Cancelar'. La cancelación es gratuita hasta 24 horas antes de la cita.",
        "help_center.faq2_question" to "¿Cómo edito mi perfil?",
        "help_center.faq2_answer" to "Ve a Ajustes > Mi cuenta > Editar perfil para actualizar tu información personal.",
        "help_center.faq3_question" to "¿Cómo contacto a un salón?",
        "help_center.faq3_answer" to "En la página del salón encontrarás la información de contacto: teléfono, email y dirección.",
        "help_center.faq4_question" to "¿Son seguros los pagos?",
        "help_center.faq4_answer" to "Sí, todos los pagos son procesados por Stripe, líder mundial en pagos online. Tu información bancaria nunca se almacena en nuestros servidores.",
        
        // ========================================
        // CONTACTAR SOPORTE
        // ========================================
        "contact.title" to "Contactar soporte",
        "contact.response_time" to "Normalmente respondemos en 24-48 horas.",
        "contact.send_message" to "Enviar un mensaje",
        "contact.subject" to "Asunto",
        "contact.message" to "Tu mensaje",
        "contact.send" to "Enviar",
        "contact.sending" to "Enviando...",
        
        // ========================================
        // TÉRMINOS DE SERVICIO
        // ========================================
        "terms.title" to "Términos de servicio",
        "terms.last_update" to "Última actualización: enero 2025",
        "terms.section1_title" to "1. Aceptación de los términos",
        "terms.section1_content" to "Al usar la aplicación Frollot, aceptas estos términos de servicio. Si no estás de acuerdo, por favor no uses la aplicación.",
        "terms.section2_title" to "2. Descripción del servicio",
        "terms.section2_content" to "Frollot es una plataforma que conecta peluquerías con sus clientes. Permite la reserva online, el pago y el seguimiento de citas.",
        "terms.section3_title" to "3. Cuenta de usuario",
        "terms.section3_content" to "Eres responsable de mantener la confidencialidad de tus credenciales de acceso y de todas las actividades que ocurran bajo tu cuenta.",
        "terms.section4_title" to "4. Pagos y cancelaciones",
        "terms.section4_content" to "Los pagos se procesan de forma segura a través de Stripe. Las políticas de cancelación varían según cada salón.",
        
        // ========================================
        // POLÍTICA DE PRIVACIDAD
        // ========================================
        "privacy.title" to "Política de privacidad",
        "privacy.last_update" to "Última actualización: enero 2025",
        "privacy.section1_title" to "1. Datos recopilados",
        "privacy.section1_content" to "Recopilamos los datos que nos proporcionas (nombre, email, teléfono) así como datos de uso de la aplicación para mejorar nuestros servicios.",
        "privacy.section2_title" to "2. Uso de los datos",
        "privacy.section2_content" to "Tus datos se utilizan para gestionar tus reservas, enviarte notificaciones importantes y mejorar tu experiencia de usuario.",
        "privacy.section3_title" to "3. Compartir datos",
        "privacy.section3_content" to "Compartimos tu información de reserva con los salones correspondientes. Nunca vendemos tus datos personales a terceros.",
        "privacy.section4_title" to "4. Tus derechos",
        "privacy.section4_content" to "Tienes derecho a acceder, modificar o eliminar tus datos personales en cualquier momento a través de la configuración de tu cuenta.",
        
        // ========================================
        // PANTALLA DE PAGO
        // ========================================
        "payment.title" to "Pago",
        "payment.cancel" to "Cancelar",
        "payment.service" to "Servicio:",
        "payment.salon" to "Salón:",
        "payment.date" to "Fecha:",
        
        // ========================================
        // PANTALLA DE SOLICITUD DE VERIFICACIÓN
        // ========================================
        "request_verification.title" to "Solicitar verificación",
        "request_verification.header_title" to "Solicitud de verificación",
        "request_verification.description" to "Selecciona el tipo de verificación que deseas obtener. Nuestro equipo revisará tu solicitud en breve.",
        "request_verification.verification_type" to "Tipo de verificación *",
        "request_verification.additional_info" to "Información adicional (opcional)",
        "request_verification.additional_info_placeholder" to "Describe tu situación, proporciona documentos (SIRET, diplomas, etc.)...",
        
        // ========================================
        // PANTALLA DE CREACIÓN DE RESEÑA
        // ========================================
        "create_review.title" to "Dejar una reseña",
        "create_review.your_booking" to "Tu reserva",
        "create_review.salon" to "Salón: {salonName}",
        "create_review.date" to "Fecha: {date}",
        "create_review.rating_question" to "¿Cómo calificarías tu experiencia?",
        "create_review.title_label" to "Título de tu reseña (opcional)",
        "create_review.title_placeholder" to "Ej: ¡Gran experiencia!",
        "create_review.comment_label" to "Tu comentario (opcional)",
        "create_review.comment_placeholder" to "Comparte tu experiencia...",
        
        // ========================================
        // PANTALLA DE GESTIÓN DE CITAS DEL PROPIETARIO
        // ========================================
        "owner_bookings_management.title" to "Gestión de Citas",
        "owner_bookings_management.confirm" to "Confirmar",
        "owner_bookings_management.start" to "Iniciar",
        "owner_bookings_management.absent" to "Ausente",
        "owner_bookings_management.finish" to "Terminar",
        
        // ========================================
        // PANTALLA DE BÚSQUEDA
        // ========================================
        "search.title" to "Buscar",
        "search.advanced_filters" to "Filtros avanzados",
        "search.post_type" to "Tipo de publicación",
        "search.search_placeholder" to "Buscar publicaciones, salones, usuarios...",
        "search.no_posts_found" to "No se encontraron publicaciones",
        "search.no_salons_found" to "No se encontraron salones",
        "search.no_users_found" to "No se encontraron usuarios",
        "search.no_hashtags_found" to "No se encontraron hashtags",
        "search.no_results_found" to "No se encontraron resultados",
        "search.all" to "Todos",
        "search.load_more" to "Cargar más",
        "search.posts" to "Publicaciones ({count})",
        "search.salons" to "Salones ({count})",
        "search.users" to "Usuarios ({count})",
        "search.hashtags" to "Hashtags ({count})",
        
        // ========================================
        // COMPONENTE ULTRA PREMIUM POST CARD
        // ========================================
        "ultra_premium_post_card.oops" to "¡Ups!",
        "ultra_premium_post_card.retry" to "Reintentar",
        "ultra_premium_post_card.no_posts_yet" to "Aún no hay publicaciones",
        "ultra_premium_post_card.be_first_to_share" to "¡Sé el primero en compartir\nalgo increíble!",
        "ultra_premium_post_card.view_comments" to "Ver comentario",
        "ultra_premium_post_card.view_comments_plural" to "Ver {count} comentarios",
        "ultra_premium_post_card.add_comment" to "Agregar un comentario...",
        "ultra_premium_post_card.pinned" to "Fijado",
        
        // ========================================
        // BOTONES Y ACCIONES COMUNES
        // ========================================
        "common.cancel" to "Cancelar",
        "common.save" to "Guardar",
        "common.confirm" to "Confirmar",
        "common.retry" to "Reintentar",
        "common.delete" to "Eliminar",
        "common.edit" to "Editar",
        "common.remove" to "Retirar",
        "common.add" to "Agregar",
        "common.create" to "Crear",
        "common.load_more" to "Cargar más",
        "common.all" to "Todos",
        "common.start" to "Iniciar",
        "common.finish" to "Terminar",
        "common.absent" to "Ausente",
        "common.popular" to "Popular",
        "common.public" to "Público",
        "common.service" to "Servicio:",
        "common.salon" to "Salón:",
        "common.date" to "Fecha:",
        "common.verified" to "Verificado",
        
        // ========================================
        // COMPONENTES UI
        // ========================================
        // RatingBar
        "components.rating_bar.star_filled" to "Estrella {number} llena",
        "components.rating_bar.star_empty" to "Estrella {number} vacía",
        "components.rating_bar.reviews_count" to "({count} reseñas)",
        
        // PasswordTextField
        "components.password_text_field.label" to "Contraseña",
        "components.password_text_field.placeholder" to "Ingrese su contraseña",
        "components.password_text_field.show_password" to "Mostrar contraseña",
        "components.password_text_field.hide_password" to "Ocultar contraseña",
        
        // ReportDialog
        "components.report_dialog.title" to "Reportar este {entity}",
        "components.report_dialog.info_message" to "Su reporte será revisado por nuestro equipo de moderación. Gracias por ayudarnos a mantener una comunidad respetuosa.",
        "components.report_dialog.reason_label" to "Razón del reporte *",
        "components.report_dialog.additional_info_label" to "Información adicional (opcional)",
        "components.report_dialog.additional_info_placeholder" to "Describa brevemente el problema...",
        "components.report_dialog.error_select_reason" to "Por favor seleccione una razón",
        "components.report_dialog.error_reporting" to "Error al reportar: {error}",
        "components.report_dialog.error_unknown" to "Error desconocido",
        
        // UserAvatar
        "components.user_avatar.content_description" to "Avatar de {name}",
        
        // QueueStatusCard
        "components.queue_status_card.connection_lost" to "Conexión perdida",
        "components.queue_status_card.data_stale" to "Datos obsoletos",
        "components.queue_status_card.last_update" to "Última actualización: hace {minutes} min",
        "components.queue_status_card.your_progress" to "Su progreso",
        "components.queue_status_card.current_position" to "Posición actual",
        "components.queue_status_card.estimated_time" to "Tiempo estimado",
        "components.queue_status_card.just_now" to "Ahora mismo",
        "components.queue_status_card.minutes_ago" to "Hace {minutes} min",
        "components.queue_status_card.leave_queue" to "Salir de la cola",
        "components.queue_status_card.reconnecting" to "Reconexión automática en curso...",
        "components.queue_status_card.keep_app_open" to "Mantenga la aplicación abierta para ser notificado por el salón.",
        "components.queue_status_card.status_offline" to "❌ Sin conexión",
        "components.queue_status_card.status_pending" to "⏸️ Actualización pendiente...",
        "components.queue_status_card.status_auto_refresh" to "✅ Actualización automática (30s)",
        
        // FullScreenImageViewer
        "components.full_screen_image_viewer.image_content_description" to "Imagen {number}",
        "components.full_screen_image_viewer.close" to "Cerrar",
        
        // AppDrawer
        "components.app_drawer.marketplace" to "Marketplace",
        "components.app_drawer.social" to "Social",
        "components.app_drawer.appointments" to "Citas",
        "components.app_drawer.account" to "Cuenta",
        "components.app_drawer.profile" to "Perfil",
        "components.app_drawer.notifications" to "Notificaciones",
        "components.app_drawer.favorites" to "Favoritos",
        "components.app_drawer.archives" to "Archivos",
        "components.app_drawer.collections" to "Colecciones",
        "components.app_drawer.management" to "Gestión",
        "components.app_drawer.my_salons" to "Mis Salones",
        "components.app_drawer.new_salon" to "Nuevo Salón",
        "components.app_drawer.create_post" to "Crear una publicación",
        "components.app_drawer.bookings_management" to "Gestión de Citas",
        "components.app_drawer.stats" to "Estadísticas",
        "components.app_drawer.activity" to "Actividad",
        "components.app_drawer.my_portfolios" to "Mis Portafolios",
        "components.app_drawer.new_portfolio" to "Nuevo Portafolio",
        "components.app_drawer.services" to "Servicios",
        "components.app_drawer.agenda" to "Agenda",
        "components.app_drawer.admin" to "Admin",
        "components.app_drawer.dashboard" to "Panel",
        "components.app_drawer.users" to "Usuarios",
        "components.app_drawer.settings" to "Configuración",
        "components.app_drawer.help" to "Ayuda",
        "components.app_drawer.guest" to "Invitado",
        "components.app_drawer.client" to "Cliente",
        "components.app_drawer.owner" to "Propietario",
        "components.app_drawer.hairstylist" to "Peluquero",
        "components.app_drawer.admin_user" to "Admin",
        
        // ========================================
        // PANTALLA DE PAGO
        // ========================================
        "payment.title" to "Pago",
        "payment.cancel" to "Cancelar",
        "payment.service" to "Servicio",
        "payment.salon" to "Salón",
        "payment.date" to "Fecha",
        "payment.card_input" to "Tarjeta",
        "payment.confirmation" to "Confirmación",
        "payment.processing" to "Procesando...",
        "payment.success" to "Pago exitoso",
        "payment.error" to "Error",
        "payment.continue" to "Continuar",
        "payment.retry" to "Reintentar",
        "payment.pay_amount" to "Pagar {amount}€",
        "payment.verify_order" to "Verificar pedido",
        "payment.booking_details" to "Detalles de la reserva",
        "payment.card" to "Tarjeta",
        "payment.total_to_pay" to "Total a pagar",
        "payment.modify_card" to "Modificar tarjeta",
        "payment.processing_message" to "Por favor espere mientras procesamos su pago de forma segura.",
        "payment.success_message" to "Su reserva está confirmada. Recibirá un correo de confirmación.",
        "payment.error_message" to "Ocurrió un error durante el pago. Por favor intente de nuevo.",
        "payment.view_booking" to "Ver mi reserva",
        "payment.card_number" to "Número de tarjeta",
        "payment.card_holder" to "Nombre en la tarjeta",
        "payment.expiry" to "Vencimiento",
        "payment.cvv" to "CVV",
        "payment.ssl" to "SSL 256-bit",
        "payment.pci_dss" to "PCI-DSS",
        "payment.3d_secure" to "3D Secure",
        "payment.history" to "Historial de pagos",
        "payment.no_payments" to "Aún no has realizado ningún pago.",
        "payment.summary" to "Resumen",
        "payment.total_spent" to "Total gastado",
        "payment.transactions" to "Transacciones",
        "payment.success_rate" to "Tasa de éxito",
        "payment.all_payments" to "Todos",
        "payment.succeeded" to "Exitosos",
        "payment.failed" to "Fallidos",
        "payment.refunded" to "Reembolsados",
        "payment.transaction_id" to "ID de transacción",
        "payment.stripe_reference" to "Referencia Stripe",
        "payment.payment_method" to "Método de pago",
        "payment.currency" to "Moneda",
        "payment.refunded_amount" to "Monto reembolsado",
        
        // Stripe Checkout
        "payment.redirect" to "Redireccionando",
        "payment.secure_payment" to "Pago seguro",
        "payment.secure_payment_description" to "Serás redirigido a la página de pago segura de Stripe.",
        "payment.order_summary" to "Resumen del pedido",
        "payment.total" to "Total",
        "payment.loading" to "Cargando...",
        "payment.proceed_to_payment" to "Proceder al pago",
        "payment.secure_ssl" to "SSL 256-bit",
        "payment.stripe_secure" to "Stripe",
        "payment.pci_compliant" to "PCI-DSS",
        "payment.redirect_to_stripe" to "Página de pago",
        "payment.redirect_description" to "Completa tu pago en la página de Stripe.\nVuelve aquí después del pago.",
        "payment.open_payment_page" to "Abrir página de pago",
        "payment.check_payment_status" to "Verificar el pago",
        "payment.processing_payment" to "Procesando...",
        "payment.processing_description" to "Por favor espera mientras verificamos tu pago.",
        "payment.payment_successful" to "¡Pago exitoso! 🎉",
        "payment.payment_success_description" to "Tu reserva está confirmada.\nRecibirás un email de confirmación.",
        "payment.payment_failed" to "Pago fallido",
        "payment.payment_failed_description" to "Ocurrió un error durante el pago.\nPor favor intenta de nuevo.",
        "payment.step_summary" to "Resumen",
        "payment.step_payment" to "Pago",
        "payment.step_confirmation" to "Confirmación",
        
        // ExternalShareDialog
        "components.external_share_dialog.title" to "Compartir a",
        "components.external_share_dialog.share_via_app" to "Compartir a través de una aplicación",
        "components.external_share_dialog.share_via_app_description" to "Instagram, WhatsApp, Mensajes, etc.",
        "components.external_share_dialog.error_sharing" to "Error al compartir: {error}",
        "components.external_share_dialog.copy_link" to "Copiar enlace",
        "components.external_share_dialog.copy_link_description" to "Copiar el enlace de la publicación al portapapeles",
        "components.external_share_dialog.error_copying" to "Error al copiar: {error}",
        "components.external_share_dialog.not_available" to "El intercambio externo no está disponible en esta plataforma",
        
        // SearchTextField
        "components.search_text_field.placeholder" to "Buscar...",
        "components.search_text_field.content_description" to "Buscar",
        
        // ========================================
        // ENUMS - LOCALIZACIÓN
        // ========================================
        // BookingStatus
        "enums.booking_status.pending" to "Pendiente",
        "enums.booking_status.confirmed" to "Confirmada",
        "enums.booking_status.in_progress" to "En curso",
        "enums.booking_status.completed" to "Completada",
        "enums.booking_status.cancelled" to "Cancelada",
        "enums.booking_status.no_show" to "Ausencia",
        
        // PaymentStatus
        "enums.payment_status.pending" to "Pendiente",
        "enums.payment_status.processing" to "Procesando",
        "enums.payment_status.succeeded" to "Exitoso",
        "enums.payment_status.failed" to "Fallido",
        "enums.payment_status.canceled" to "Cancelado",
        "enums.payment_status.partially_refunded" to "Reembolsado parcialmente",
        "enums.payment_status.unpaid" to "No pagado",
        "enums.payment_status.paid" to "Pagado",
        "enums.payment_status.refunded" to "Reembolsado",
        
        // PostType
        "enums.post_type.general" to "General",
        "enums.post_type.avant_apres" to "Antes/Después",
        "enums.post_type.portfolio" to "Portafolio",
        "enums.post_type.tendance" to "Tendencia",
        "enums.post_type.conseil" to "Consejo",
        "enums.post_type.realisation" to "Realización",
        "enums.post_type.inspiration" to "Inspiración",
        "enums.post_type.general_description" to "Publicación general",
        "enums.post_type.avant_apres_description" to "Mostrar una transformación antes/después",
        "enums.post_type.portfolio_description" to "Agregar a su portafolio",
        "enums.post_type.tendance_description" to "Compartir una tendencia de peinado",
        "enums.post_type.conseil_description" to "Dar consejos y trucos",
        "enums.post_type.realisation_description" to "Mostrar una realización",
        "enums.post_type.inspiration_description" to "Compartir una inspiración",
        
        // PostVisibility
        "enums.post_visibility.public" to "Público",
        "enums.post_visibility.followers" to "Solo seguidores",
        "enums.post_visibility.private" to "Privado",
        "enums.post_visibility.public_description" to "Visible para todos",
        "enums.post_visibility.followers_description" to "Visible solo para sus seguidores",
        "enums.post_visibility.private_description" to "Visible solo para usted",
        
        // ServiceCategory
        "enums.service_category.coupe" to "Corte y Talla",
        "enums.service_category.coloration" to "Coloración",
        "enums.service_category.soin" to "Cuidados",
        "enums.service_category.coiffage" to "Peinado",
        "enums.service_category.barbe" to "Barbero",
        "enums.service_category.technique" to "Técnicas Especiales",
        "enums.service_category.autre" to "Otros Servicios",
        
        // ReactionType
        "enums.reaction_type.like" to "Me gusta",
        "enums.reaction_type.love" to "Me encanta",
        "enums.reaction_type.wow" to "Wow",
        "enums.reaction_type.inspirant" to "Inspirador",
        "enums.reaction_type.magnifique" to "Magnífico",
        "enums.reaction_type.bravo" to "Bravo",
        "enums.reaction_type.like_description" to "Me gusta clásico",
        "enums.reaction_type.love_description" to "¡Me encanta este color!",
        "enums.reaction_type.wow_description" to "¡Transformación increíble!",
        "enums.reaction_type.inspirant_description" to "¡Quiero lo mismo!",
        "enums.reaction_type.magnifique_description" to "¡Trabajo de calidad!",
        "enums.reaction_type.bravo_description" to "¡Felicitaciones al peluquero!",
        
        // MediaType
        "enums.media_type.before" to "Antes",
        "enums.media_type.after" to "Después",
        "enums.media_type.process" to "Proceso",
        "enums.media_type.detail" to "Detalle",
        
        // ReportReason
        "enums.report_reason.inapproprie" to "Contenido inapropiado",
        "enums.report_reason.spam" to "Spam publicitario",
        "enums.report_reason.faux" to "Falso antes/después",
        "enums.report_reason.copyright" to "Violación de derechos de autor",
        "enums.report_reason.autre" to "Otro",
        "enums.report_reason.inapproprie_description" to "Contenido violento, acosador u ofensivo",
        "enums.report_reason.spam_description" to "Publicidad no solicitada o contenido repetitivo",
        "enums.report_reason.faux_description" to "Transformación o resultado engañoso",
        "enums.report_reason.copyright_description" to "Uso no autorizado de contenido protegido",
        "enums.report_reason.autre_description" to "Otra razón a especificar",
        
        // ReportedEntityType
        "enums.reported_entity_type.post" to "publicación",
        "enums.reported_entity_type.comment" to "comentario",
        "enums.reported_entity_type.user" to "usuario",
        "enums.reported_entity_type.salon" to "salón",
        
        // VerificationType
        "enums.verification_type.email" to "Email verificado",
        "enums.verification_type.phone" to "Teléfono verificado",
        "enums.verification_type.business" to "Empresa verificada",
        "enums.verification_type.professional" to "Profesional verificado",
        "enums.verification_type.email_description" to "Email verificado por confirmación",
        "enums.verification_type.phone_description" to "Número de teléfono verificado",
        "enums.verification_type.business_description" to "Empresa verificada (SIRET, documentos)",
        "enums.verification_type.professional_description" to "Diplomas y certificaciones verificados",
        
        // BadgeCategory
        "enums.badge_category.certification" to "Certificación",
        "enums.badge_category.competition" to "Competición",
        "enums.badge_category.formation" to "Formación",
        "enums.badge_category.partenariat" to "Asociación",
        
        // ReportStatus
        "enums.report_status.pending" to "Pendiente",
        "enums.report_status.reviewed" to "En revisión",
        "enums.report_status.resolved" to "Resuelto",
        "enums.report_status.dismissed" to "Descartado",
        
        // ModerationAction
        "enums.moderation_action.hide" to "Ocultar",
        "enums.moderation_action.delete" to "Eliminar",
        "enums.moderation_action.warn" to "Advertir",
        "enums.moderation_action.hide_description" to "El contenido será ocultado para todos los usuarios excepto el autor y los administradores.",
        "enums.moderation_action.delete_description" to "El contenido será eliminado permanentemente y no podrá ser restaurado.",
        "enums.moderation_action.warn_description" to "Se enviará una advertencia al autor sin modificar el contenido.",

        // AppealStatus
        "enums.appeal_status.none" to "Sin apelación",
        "enums.appeal_status.pending" to "Pendiente",
        "enums.appeal_status.approved" to "Aprobado",
        "enums.appeal_status.rejected" to "Rechazado",

        // ModerationAppealStatus
        "enums.appeal_status.none" to "Sin apelación",
        "enums.appeal_status.pending" to "Pendiente",
        "enums.appeal_status.approved" to "Aprobado",
        "enums.appeal_status.rejected" to "Rechazado",
        
        // HairHashtagCategory
        "enums.hair_hashtag_category.technique" to "Técnica",
        "enums.hair_hashtag_category.style" to "Estilo",
        "enums.hair_hashtag_category.couleur" to "Color",
        "enums.hair_hashtag_category.longueur" to "Longitud",
        "enums.hair_hashtag_category.texture" to "Textura",
        
        // FollowingType
        "enums.following_type.coiffeur" to "Peluquero",
        "enums.following_type.salon" to "Salón",
    )
    
    return StringsBundle(strings)
}
