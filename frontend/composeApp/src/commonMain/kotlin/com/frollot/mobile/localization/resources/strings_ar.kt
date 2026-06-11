package com.frollot.mobile.localization.resources

/**
 * Bundle من النصوص العربية.
 * 
 * ترجمات كاملة لجميع شاشات ومكونات التطبيق.
 * ملاحظة: العربية لغة RTL (من اليمين إلى اليسار).
 */
fun createArabicStrings(): StringsBundle {
    val strings = mapOf<String, String>(
        // ========================================
        // شاشة تسجيل الدخول
        // ========================================
        "login.welcome_title" to "مرحباً 👋",
        "login.welcome_subtitle" to "قم بتسجيل الدخول واستكشف\nإمكانيات لا حصر لها",
        "login.email_label" to "عنوان البريد الإلكتروني",
        "login.email_placeholder" to "you@example.com",
        "login.password_label" to "كلمة المرور",
        "login.password_placeholder" to "••••••••",
        "login.forgot_password" to "نسيت كلمة المرور؟",
        "login.submit_button" to "تسجيل الدخول",
        "login.submit_button_loading" to "جاري تسجيل الدخول...",
        "login.continue_with" to "أو تابع مع",
        "login.google_button" to "Google",
        "login.facebook_button" to "Facebook",
        "login.new_here" to "جديد هنا؟",
        "login.create_account" to "إنشاء حساب",
        "login.secure_connection" to "اتصال آمن 100%",
        
        // رسائل خطأ تسجيل الدخول
        "login.errors.invalid_credentials" to "البريد الإلكتروني أو كلمة المرور غير صحيحة",
        "login.errors.account_disabled" to "الحساب معطل. يرجى الاتصال بالدعم.",
        "login.errors.account_not_found" to "الحساب غير موجود",
        "login.errors.server_unavailable" to "تعذر الاتصال بالخادم",
        "login.errors.timeout" to "انتهت مهلة الانتظار. تحقق من اتصالك بالإنترنت.",
        "login.errors.generic_error" to "خطأ في تسجيل الدخول",
        
        // ========================================
        // شاشة التسجيل
        // ========================================
        "register.welcome_title" to "انضم إلينا 🎉",
        "register.welcome_subtitle" to "أنشئ حسابك في ثوانٍ",
        "register.first_name_label" to "الاسم الأول",
        "register.first_name_placeholder" to "صوفي",
        "register.last_name_label" to "اسم العائلة",
        "register.last_name_placeholder" to "مارتن",
        "register.email_label" to "عنوان البريد الإلكتروني",
        "register.email_placeholder" to "you@example.com",
        "register.account_type_label" to "نوع الحساب",
        "register.password_label" to "كلمة المرور",
        "register.password_placeholder" to "8 أحرف على الأقل",
        "register.confirm_password_label" to "تأكيد كلمة المرور",
        "register.confirm_password_placeholder" to "أعد إدخال كلمة المرور",
        "register.password_mismatch" to "كلمات المرور غير متطابقة",
        "register.submit_button" to "إنشاء حسابي",
        "register.submit_button_loading" to "جاري إنشاء الحساب...",
        "register.sign_up_with" to "أو سجل مع",
        "register.google_button" to "Google",
        "register.facebook_button" to "Facebook",
        "register.already_registered" to "مسجل بالفعل؟",
        "register.login_link" to "تسجيل الدخول",
        "register.data_protected" to "بياناتك محمية",
        "register.success_message" to "مرحباً {firstName} {lastName}! تم إنشاء حسابك بنجاح.",
        
        // أنواع الحساب
        "register.user_types.client" to "👤 عميل",
        "register.user_types.hairstylist" to "✂️ مصفف شعر",
        "register.user_types.salon_owner" to "🏢 مالك صالون",
        "register.user_types.admin" to "⚙️ مسؤول",
        
        // رسائل خطأ التسجيل
        "register.errors.email_already_used" to "هذا البريد الإلكتروني مستخدم بالفعل",
        "register.errors.invalid_data" to "بيانات غير صحيحة، تحقق من معلوماتك",
        "register.errors.server_unavailable" to "تعذر الاتصال بالخادم",
        "register.errors.timeout" to "انتهت مهلة الانتظار. تحقق من اتصالك بالإنترنت.",
        "register.errors.generic_error" to "خطأ في التسجيل",
        
        // ========================================
        // الشاشة الرئيسية
        // ========================================
        "home.title" to "Frollot",
        "home.filters_button" to "المرشحات",
        "home.my_bookings_button" to "حجوزاتي",
        "home.create_salon_button" to "إنشاء صالون",
        "home.loading_message" to "جاري تحميل الصالونات",
        "home.error_message" to "تعذر تحميل الصالونات",
        "home.empty_state_title" to "لا توجد صالونات متاحة",
        "home.empty_state_title_with_filters" to "لا يوجد صالون يطابق معاييرك",
        "home.empty_state_message" to "كن أول من ينشئ صالوناً!",
        "home.empty_state_message_with_filters" to "حاول توسيع بحثك أو إزالة بعض المرشحات.",
        "home.create_my_salon" to "إنشاء صالوني",
        "home.premium_salons" to "صالونات مميزة",
        "home.near_me" to "بالقرب مني",
        "home.city" to "المدينة",
        "home.salons_found" to "تم العثور على {count} صالون",
        "home.salons_found_plural" to "تم العثور على {count} صالون",
        "home.filter_by_city" to "تصفية حسب المدينة",
        "home.filter_by_city_description" to "أدخل مدينة لتصفية الصالونات.",
        "home.city_placeholder" to "مثال: باريس",
        "home.apply" to "تطبيق",
        "home.reset" to "إعادة تعيين",
        "home.premium_badge" to "مميز",
        "home.rating" to "التقييم",
        "home.open_hours" to "مفتوح • 9ص-7م",
        "home.cut" to "قص",
        "home.coloring" to "صبغة",
        "home.book" to "حجز",
        "home.cover_photo_description" to "صورة الغلاف لـ {salonName}",
        
        // ========================================
        // شاشة الإعدادات
        // ========================================
        "settings.title" to "الإعدادات",
        
        // الأقسام
        "settings.sections.account" to "الحساب",
        "settings.sections.privacy" to "الخصوصية",
        "settings.sections.notifications" to "الإشعارات",
        "settings.sections.appearance" to "المظهر",
        "settings.sections.social_network" to "الشبكة الاجتماعية",
        "settings.sections.bookings" to "الحجوزات",
        "settings.sections.content_and_media" to "المحتوى والوسائط",
        "settings.sections.data_privacy" to "خصوصية البيانات",
        "settings.sections.help_and_support" to "المساعدة والدعم",
        "settings.sections.about" to "حول",
        
        // الحساب
        "settings.account.profile" to "الملف الشخصي",
        "settings.account.profile_subtitle" to "إدارة ملفك الشخصي ومعلوماتك",
        "settings.account.security" to "الأمان",
        "settings.account.security_subtitle" to "كلمة المرور، المصادقة الثنائية",
        "settings.account.email" to "البريد الإلكتروني",
        "settings.account.phone" to "الهاتف",
        "settings.account.phone_subtitle" to "إضافة رقم هاتف",
        "settings.account.not_defined" to "غير محدد",
        
        // الخصوصية
        "settings.privacy.profile_visibility" to "رؤية الملف الشخصي",
        "settings.privacy.who_can_follow_me" to "من يمكنه متابعتي",
        "settings.privacy.who_can_message_me" to "من يمكنه إرسال رسائل لي",
        "settings.privacy.show_activity_status" to "إظهار حالة النشاط",
        "settings.privacy.show_activity_status_subtitle" to "سيتمكن المستخدمون الآخرون من رؤية متى تكون متصلًا",
        "settings.privacy.blocked_users" to "المستخدمون المحظورون",
        "settings.privacy.blocked_users_count" to "{count} مستخدم",
        "settings.privacy.no_blocked_users" to "لا يوجد مستخدمون محظورون",
        "settings.privacy.options.public" to "عام",
        "settings.privacy.options.followers_only" to "المتابعون فقط",
        "settings.privacy.options.private" to "خاص",
        "settings.privacy.options.everyone" to "الجميع",
        "settings.privacy.options.nobody" to "لا أحد",
        
        // الإشعارات
        "settings.notifications.title" to "الإشعارات",
        "settings.notifications.subtitle" to "تفعيل أو إلغاء تفعيل جميع الإشعارات",
        "settings.notifications.push" to "الإشعارات الفورية",
        "settings.notifications.push_subtitle" to "تلقي الإشعارات على جهازك",
        "settings.notifications.email" to "الإشعارات عبر البريد الإلكتروني",
        "settings.notifications.email_subtitle" to "تلقي الإشعارات عبر البريد الإلكتروني",
        "settings.notifications.bookings" to "الحجوزات",
        "settings.notifications.bookings_subtitle" to "الإشعارات لمواعيدك",
        "settings.notifications.social" to "الشبكة الاجتماعية",
        "settings.notifications.social_subtitle" to "الإعجابات، التعليقات، الإشارات، إلخ.",
        "settings.notifications.marketing" to "التسويق",
        "settings.notifications.marketing_subtitle" to "العروض الخاصة والأخبار",
        
        // المظهر
        "settings.appearance.dark_mode" to "الوضع الداكن",
        "settings.appearance.dark_mode_subtitle" to "تفعيل المظهر الداكن",
        "settings.appearance.language" to "اللغة",
        
        // الشبكة الاجتماعية
        "settings.social_network.post_visibility_default" to "رؤية المنشورات الافتراضية",
        "settings.social_network.allow_comments" to "السماح بالتعليقات",
        "settings.social_network.allow_comments_subtitle" to "يمكن للمستخدمين الآخرين التعليق على منشوراتك",
        "settings.social_network.allow_reactions" to "السماح بالتفاعلات",
        "settings.social_network.allow_reactions_subtitle" to "يمكن للمستخدمين الآخرين التفاعل مع منشوراتك",
        "settings.social_network.allow_shares" to "السماح بالمشاركة",
        "settings.social_network.allow_shares_subtitle" to "يمكن للمستخدمين الآخرين مشاركة منشوراتك",
        
        // الحجوزات
        "settings.bookings.booking_notifications" to "إشعارات الحجوزات",
        "settings.bookings.booking_notifications_subtitle" to "تلقي الإشعارات لمواعيدك",
        "settings.bookings.availability_preferences" to "تفضيلات التوفر",
        "settings.bookings.availability_preferences_subtitle" to "إدارة الأوقات المفضلة لديك",
        "settings.bookings.payment_methods" to "طرق الدفع",
        "settings.bookings.payment_methods_subtitle" to "إدارة طرق الدفع الخاصة بك",
        
        // المحتوى والوسائط
        "settings.content_and_media.auto_save_photos" to "حفظ الصور تلقائياً",
        "settings.content_and_media.auto_save_photos_subtitle" to "حفظ الصور في معرضك",
        "settings.content_and_media.data_usage" to "استخدام البيانات",
        "settings.content_and_media.video_quality" to "جودة الفيديو",
        "settings.content_and_media.data_usage_options.economical" to "اقتصادي",
        "settings.content_and_media.data_usage_options.standard" to "قياسي",
        "settings.content_and_media.data_usage_options.high" to "عالي",
        "settings.content_and_media.video_quality_options.sd" to "SD",
        "settings.content_and_media.video_quality_options.hd" to "HD",
        "settings.content_and_media.video_quality_options.full_hd" to "Full HD",
        
        // خصوصية البيانات
        "settings.data_privacy.download_data" to "تنزيل بياناتك",
        "settings.data_privacy.download_data_subtitle" to "الحصول على نسخة من بياناتك",
        "settings.data_privacy.delete_account" to "حذف حسابك",
        "settings.data_privacy.delete_account_subtitle" to "حذف حسابك وبياناتك نهائياً",
        
        // المساعدة والدعم
        "settings.help_and_support.help_center" to "مركز المساعدة",
        "settings.help_and_support.help_center_subtitle" to "الأسئلة الشائعة وأدلة الاستخدام",
        "settings.help_and_support.contact_us" to "اتصل بنا",
        "settings.help_and_support.contact_us_subtitle" to "الإبلاغ عن مشكلة أو طرح سؤال",
        "settings.help_and_support.report_bug" to "الإبلاغ عن خطأ",
        "settings.help_and_support.report_bug_subtitle" to "ساعدنا في تحسين التطبيق",
        "settings.help_and_support.rate_app" to "تقييم التطبيق",
        "settings.help_and_support.rate_app_subtitle" to "شارك رأيك في المتجر",
        
        // حول
        "settings.about.version" to "الإصدار",
        "settings.about.terms_of_service" to "شروط الخدمة",
        "settings.about.terms_of_service_subtitle" to "قراءة شروط الخدمة",
        "settings.about.privacy_policy" to "سياسة الخصوصية",
        "settings.about.privacy_policy_subtitle" to "قراءة سياسة الخصوصية",
        "settings.about.licenses" to "التراخيص",
        "settings.about.licenses_subtitle" to "تراخيص مفتوحة المصدر",
        
        // الإجراءات
        "settings.actions.logout" to "تسجيل الخروج",
        "settings.actions.logout_dialog_title" to "تسجيل الخروج",
        "settings.actions.logout_dialog_text" to "هل أنت متأكد من أنك تريد تسجيل الخروج؟",
        "settings.actions.delete" to "حذف",
        "settings.actions.delete_account_dialog_title" to "حذف حسابك",
        "settings.actions.delete_account_dialog_text" to "هذا الإجراء لا يمكن التراجع عنه. سيتم حذف جميع بياناتك نهائياً. هل أنت متأكد تماماً؟",
        "settings.actions.cancel" to "إلغاء",
        
        // ========================================
        // شاشة الملف الشخصي
        // ========================================
        "profile.version" to "الإصدار 1.0.0",
        "profile.logout_dialog_title" to "تسجيل الخروج",
        "profile.logout_dialog_text" to "هل أنت متأكد من أنك تريد تسجيل الخروج؟",
        "profile.logout_confirm" to "نعم، سجلني للخروج",
        "profile.cancel" to "إلغاء",
        "profile.avatar_preview" to "معاينة الصورة الرمزية",
        "profile.save" to "حفظ",
        "profile.edit_photo" to "تعديل الصورة",
        "profile.account_verified" to "حساب موثق",
        "profile.account_not_verified" to "غير موثق",
        "profile.account_info" to "معلومات الحساب",
        "profile.email" to "البريد الإلكتروني",
        "profile.phone" to "الهاتف",
        "profile.member_since" to "عضو منذ",
        "profile.account_type" to "نوع الحساب",
        "profile.edit_profile" to "تعديل ملفي الشخصي",
        "profile.edit_profile_subtitle" to "تغيير الاسم، الاسم الأول، الهاتف",
        "profile.change_password" to "تغيير كلمة المرور",
        "profile.change_password_subtitle" to "تحديث بيانات اعتمادك",
        "profile.logout" to "تسجيل الخروج",
        "profile.logout_subtitle" to "إنهاء جلستك",
        "profile.statistics" to "الإحصائيات",
        "profile.salons" to "الصالونات",
        "profile.bookings" to "الحجوزات",
        "profile.reviews" to "التقييمات",
        "profile.services" to "الخدمات",
        "profile.points" to "النقاط",
        "profile.user_types.client" to "عميل",
        "profile.user_types.salon_owner" to "مالك صالون",
        "profile.user_types.hairstylist" to "مصفف شعر",
        "profile.user_types.admin" to "مسؤول",
        
        // ========================================
        // شاشة الخلاصة الاجتماعية
        // ========================================
        "social_feed.filter_by_type" to "تصفية حسب النوع",
        "social_feed.my_follows" to "متابعاتي",
        "social_feed.near_me" to "بالقرب مني",
        "social_feed.all" to "الكل",
        "social_feed.add_comment" to "أضف تعليقاً (اختياري)",
        "social_feed.share" to "مشاركة",
        "social_feed.cancel" to "إلغاء",
        "social_feed.no_collection_yet" to "ليس لديك أي مجموعة بعد",
        "social_feed.save" to "حفظ",
        "social_feed.archive" to "أرشفة",
        "social_feed.share_in_app" to "مشاركة في التطبيق",
        "social_feed.share_external" to "مشاركة خارجية",
        "social_feed.report" to "الإبلاغ",
        "social_feed.add_to_collection_example" to "مثال: حجزت في هذا الصالون بفضل هذا المنشور!",
        "social_feed.add_to_collection" to "إضافة إلى المجموعة",
        "social_feed.create_collection" to "إنشاء مجموعة",
        
        // ========================================
        // شاشة حجوزاتي
        // ========================================
        "my_bookings.service" to "الخدمة",
        "my_bookings.date_time" to "التاريخ والوقت",
        "my_bookings.hairstylist" to "مصفف الشعر",
        "my_bookings.amount" to "المبلغ",
        "my_bookings.review_left" to "تم ترك تقييم",
        "my_bookings.leave_review" to "ترك تقييم",
        "my_bookings.cancel" to "إلغاء",
        "my_bookings.cancel_confirm" to "نعم، إلغاء",
        "my_bookings.cancel_keep" to "لا، الاحتفاظ",
        
        // ========================================
        // شاشة الحجز (معالج)
        // ========================================
        "booking.back_to_home" to "العودة إلى الرئيسية",
        "booking.duration" to "المدة",
        "booking.category" to "الفئة",
        "booking.choose_expert" to "اختر خبيرك",
        "booking.choose_expert_subtitle" to "اختر مصفف شعر أو اترك الصالون يختار",
        "booking.select_date" to "اختر تاريخاً",
        "booking.select_date_subtitle" to "اختر يوم موعدك",
        "booking.choose_time_slot" to "اختر وقتاً",
        "booking.choose_time_slot_subtitle" to "اختر الوقت الذي يناسبك",
        "booking.notes_or_special_requests" to "ملاحظات أو طلبات خاصة",
        "booking.notes_or_special_requests_subtitle" to "اختياري - أخبرنا عن تفضيلاتك",
        "booking.notes_info" to "سيتم نقل هذه المعلومات إلى مصفف شعرك",
        "booking.expert_selected" to "تم اختيار الخبير",
        "booking.continue" to "متابعة",
        "booking.time_slot_selected" to "تم اختيار الوقت",
        "booking.pay_now" to "ادفع الآن",
        "booking.view_my_bookings" to "عرض حجوزاتي",
        
        // ========================================
        // شاشة تفاصيل الحجز
        // ========================================
        "booking_detail.cancel_keep" to "لا، الاحتفاظ",
        "booking_detail.hair_salon" to "صالون الحلاقة",
        "booking_detail.click_for_details" to "انقر لرؤية التفاصيل",
        "booking_detail.service" to "الخدمة",
        "booking_detail.service_details" to "تفاصيل خدمتك",
        "booking_detail.booking" to "الحجز",
        "booking_detail.booking_details" to "التاريخ، الوقت ومصفف الشعر",
        "booking_detail.payment" to "الدفع",
        "booking_detail.payment_details" to "تفاصيل المعاملة",
        "booking_detail.amount" to "المبلغ",
        "booking_detail.cancel_booking_title" to "إلغاء الحجز؟",
        "booking_detail.cancel_confirm_message" to "هل أنت متأكد أنك تريد إلغاء هذا الحجز؟ هذا الإجراء لا رجعة فيه.",
        "booking_detail.booking_status" to "حالة حجزك",
        
        // ========================================
        // شاشة تفاصيل الصالون
        // ========================================
        "salon_detail.services" to "الخدمات",
        "salon_detail.view_posts" to "عرض المنشورات",
        "salon_detail.open_feed" to "فتح الخلاصة",
        "salon_detail.add_member" to "إضافة عضو",
        "salon_detail.queue" to "قائمة الانتظار",
        "salon_detail.position" to "الموضع: #{position}",
        "salon_detail.leave_queue" to "مغادرة قائمة الانتظار",
        "salon_detail.join_queue" to "الانضمام إلى قائمة الانتظار",
        "salon_detail.login_to_join_queue" to "قم بتسجيل الدخول للانضمام إلى قائمة الانتظار",
        "salon_detail.cancel" to "إلغاء",
        "salon_detail.subscribers" to "المشتركون",
        "salon_detail.book" to "حجز",
        "salon_detail.salon_verified" to "صالون موثق",
        "salon_detail.clients" to "العملاء",
        "salon_detail.waiting" to "في الانتظار",
        "salon_detail.your_position" to "موضعك",
        "salon_detail.position_in_queue" to "الرقم {position} في قائمة الانتظار",
        "salon_detail.my_team" to "فريقي",
        "salon_detail.team" to "الفريق",
        "salon_detail.our_services" to "خدماتنا",
        "salon_detail.choose_from_services" to "اختر من {count} خدمة",
        "salon_detail.choose_from_services_plural" to "اختر من {count} خدمة",
        "salon_detail.loading_services" to "جاري تحميل الخدمات",
        "salon_detail.please_wait" to "يرجى الانتظار...",
        "salon_detail.loading_error" to "خطأ في التحميل",
        "salon_detail.no_services_available" to "لا توجد خدمات متاحة",
        "salon_detail.no_services_message" to "لم يضف هذا الصالون أي خدمات بعد.\nعد لاحقاً!",
        "salon_detail.ready_to_book" to "جاهز للحجز؟",
        "salon_detail.reviews_and_ratings" to "التقييمات والملاحظات",
        
        // ========================================
        // شاشة إنشاء صالون
        // ========================================
        "create_salon.launch_your_salon" to "أطلق صالونك",
        "create_salon.join_community" to "انضم إلى مجتمعنا من المحترفين",
        "create_salon.salon_info" to "معلومات الصالون",
        "create_salon.cover_photo" to "صورة الغلاف",
        "create_salon.cover_photo_hint" to "اختياري • JPG, PNG • الحد الأقصى 10MB",
        "create_salon.add_photo" to "إضافة صورة",
        "create_salon.click_to_browse" to "انقر لتصفح ملفاتك",
        "create_salon.data_secured" to "بياناتك آمنة ومحمية",
        
        // ========================================
        // شاشة تفاصيل المنشور
        // ========================================
        "post_detail.comments" to "تعليق واحد ({count})",
        "post_detail.comments_plural" to "{count} تعليقات",
        
        // ========================================
        // شاشة إنشاء منشور
        // ========================================
        "create_post.public" to "عام",
        "create_post.post_type" to "نوع المنشور",
        "create_post.visibility" to "الرؤية",
        "create_post.at_least_two_images" to "صورة واحدة على الأقل (قبل + بعد)",
        "create_post.click_to_select_photo" to "انقر لاختيار صورة",
        "create_post.photo_format_hint" to "JPG, PNG (الحد الأقصى 10MB)",
        "create_post.post_will_be_visible" to "سيكون منشورك مرئياً لجميع المستخدمين",
        
        // ========================================
        // شاشة ملف مصفف الشعر الشخصي
        // ========================================
        "coiffeur_profile.pinned_posts" to "المنشورات المثبتة",
        "coiffeur_profile.recent_posts" to "المنشورات الأخيرة",
        "coiffeur_profile.unfollow" to "إلغاء المتابعة",
        "coiffeur_profile.follow" to "متابعة",
        "coiffeur_profile.badges_and_certifications" to "الشارات والشهادات",
        "coiffeur_profile.featured_portfolio" to "المحفظة المميزة",
        "coiffeur_profile.portfolios" to "المحافظ",
        
        // ========================================
        // شاشة الملف الشخصي الاجتماعي للصالون
        // ========================================
        "salon_social_profile.recent_posts" to "المنشورات الأخيرة",
        "salon_social_profile.unfollow" to "إلغاء المتابعة",
        "salon_social_profile.follow" to "متابعة",
        "salon_social_profile.featured_posts" to "المنشورات المميزة",
        "salon_social_profile.portfolios" to "المحافظ",
        "salon_social_profile.verified" to "موثق",
        "salon_social_profile.services" to "الخدمات",
        
        // ========================================
        // شاشة ملف العميل
        // ========================================
        "client_profile.title" to "ملف العميل",
        "client_profile.about" to "حول",
        "client_profile.posts" to "المنشورات",
        "client_profile.likes" to "الإعجابات",
        "client_profile.followers" to "المتابعون",
        "client_profile.following" to "يتابع",
        "client_profile.collections" to "المجموعات",
        "client_profile.collections_count" to "المجموعات",
        "client_profile.recent_posts" to "المنشورات الأخيرة",
        "client_profile.badges" to "الشارات",
        "client_profile.follow" to "متابعة",
        "client_profile.unfollow" to "إلغاء المتابعة",
        
        // ========================================
        // شاشة ملف مالك الصالون
        // ========================================
        "salon_owner_profile.title" to "ملف مالك الصالون",
        "salon_owner_profile.about" to "حول",
        "salon_owner_profile.posts" to "المنشورات",
        "salon_owner_profile.likes" to "الإعجابات",
        "salon_owner_profile.followers" to "المتابعون",
        "salon_owner_profile.salons" to "الصالونات",
        "salon_owner_profile.collections" to "المجموعات",
        "salon_owner_profile.collections_count" to "المجموعات",
        "salon_owner_profile.recent_posts" to "المنشورات الأخيرة",
        "salon_owner_profile.badges" to "الشارات",
        "salon_owner_profile.follow" to "متابعة",
        "salon_owner_profile.unfollow" to "إلغاء المتابعة",
        
        // ========================================
        // شاشة التعليقات
        // ========================================
        "comments.title" to "التعليقات",
        "comments.add_comment" to "إضافة تعليق...",
        "comments.load_more" to "تحميل المزيد من التعليقات",
        
        // ========================================
        // شاشة المجموعات
        // ========================================
        "collections.title" to "المجموعات",
        "collections.post" to "منشور",
        "collections.posts" to "منشورات",
        "collections.edit" to "تعديل",
        "collections.delete" to "حذف",
        "collections.new_collection" to "مجموعة جديدة",
        "collections.name" to "الاسم *",
        "collections.description" to "الوصف (اختياري)",
        "collections.category" to "الفئة",
        "collections.public_collection" to "مجموعة عامة",
        "collections.save_to_collection" to "حفظ في المجموعة",
        "collections.no_collections" to "لا توجد مجموعات. أنشئ واحدة أولاً!",
        
        // ========================================
        // شاشة تفاصيل المجموعة
        // ========================================
        "collection_detail.title" to "المجموعة",
        "collection_detail.load_more" to "تحميل المزيد",
        "collection_detail.edit" to "تعديل",
        "collection_detail.delete" to "حذف",
        "collection_detail.delete_collection" to "حذف المجموعة",
        "collection_detail.delete_collection_message" to "هل أنت متأكد أنك تريد حذف هذه المجموعة؟ سيتم إزالة جميع المنشورات.",
        "collection_detail.remove_from_collection" to "إزالة من المجموعة",
        
        // ========================================
        // شاشة إنشاء محفظة
        // ========================================
        "create_portfolio.title" to "إنشاء محفظة",
        "create_portfolio.no_salon_found" to "لم يتم العثور على صالون. يرجى إنشاء صالون أولاً.",
        "create_portfolio.salon" to "الصالون",
        "create_portfolio.cover_image" to "صورة الغلاف",
        "create_portfolio.add_cover_image" to "إضافة صورة غلاف",
        "create_portfolio.public_portfolio" to "محفظة عامة",
        "create_portfolio.name" to "اسم المحفظة *",
        "create_portfolio.name_placeholder" to "مثال: تلويناتي 2024",
        "create_portfolio.description" to "الوصف",
        
        // ========================================
        // شاشة تفاصيل المحفظة
        // ========================================
        "portfolio_detail.load_more" to "تحميل المزيد",
        
        // ========================================
        // شاشة قائمة المحافظ
        // ========================================
        "portfolios_list.create_portfolio" to "إنشاء محفظة",
        
        // ========================================
        // شاشة إنشاء خدمة
        // ========================================
        "create_service.premium_service" to "خدمة مميزة",
        "create_service.service_info" to "معلومات الخدمة",
        "create_service.category" to "الفئة *",
        "create_service.price_adjustment_info" to "يمكن تعديل الأسعار في أي وقت من مساحتك المهنية.",
        "create_service.name" to "اسم الخدمة *",
        "create_service.name_placeholder" to "مثال: قص رجالي، صبغة كاملة...",
        "create_service.description" to "الوصف",
        "create_service.duration" to "المدة (دقيقة) *",
        "create_service.price" to "السعر (€) *",
        
        // ========================================
        // شاشة منشورات الصالون
        // ========================================
        "salon_posts.title" to "منشورات {salonName}",
        "salon_posts.all" to "الكل",
        "salon_posts.all_services" to "جميع الخدمات",
        "salon_posts.popular" to "الشائعة",
        
        // ========================================
        // شاشة الأرشيف
        // ========================================
        "archives.title" to "أرشيفي",
        
        // ========================================
        // شاشة المفضلة
        // ========================================
        "favorites.title" to "مفضلاتي",
        "favorites.offline_mode" to "وضع عدم الاتصال - بيانات مخزنة مؤقتاً",
        
        // ========================================
        // شاشة الاتجاهات
        // ========================================
        "trending.title" to "الاتجاهات",
        "trending.posts" to "المنشورات",
        "trending.hashtags" to "الهاشتاقات",
        "trending.salons" to "الصالونات",
        
        // ========================================
        // شاشة الإبلاغ
        // ========================================
        "report.info_message" to "سيتم مراجعة تقريرك من قبل فريق الإشراف لدينا. شكراً لمساعدتنا في الحفاظ على مجتمع محترم.",
        "report.reason_title" to "سبب الإبلاغ *",
        "report.additional_info" to "معلومات إضافية (اختياري)",
        "report.cancel" to "إلغاء",
        "report.submit" to "الإبلاغ",
        "report.reported_content" to "المحتوى المبلغ عنه",
        "report.post_author" to "مؤلف المنشور",
        
        // ========================================
        // شاشة إنشاء موظف
        // ========================================
        "create_staff.title" to "إضافة عضو",
        "create_staff.new_collaborator" to "متعاون جديد",
        "create_staff.personal_info" to "المعلومات الشخصية",
        "create_staff.specialties" to "التخصصات",
        "create_staff.specialties_hint" to "اختر فئات الخدمات التي يمكن لهذا العضو أداؤها.",
        "create_staff.email_info" to "سيحصل المتعاون على بريد إلكتروني مع معلومات تسجيل الدخول الخاصة به.",
        "create_staff.first_name" to "الاسم الأول *",
        "create_staff.first_name_placeholder" to "مثال: أحمد",
        "create_staff.last_name" to "الاسم الأخير *",
        "create_staff.last_name_placeholder" to "مثال: محمد",
        "create_staff.email" to "البريد الإلكتروني *",
        
        // ========================================
        // شاشة إدارة قائمة الانتظار
        // ========================================
        "queue_management.title" to "{salonName} - إدارة قائمة الانتظار",
        "queue_management.ticket" to "تذكرة #{position}",
        "queue_management.arrived" to "وصل",
        "queue_management.estimated_wait" to "وقت الانتظار المتوقع",
        "queue_management.call_next" to "استدعاء التالي",
        "queue_management.remove" to "إزالة",
        
        // ========================================
        // شاشة تغيير البريد الإلكتروني
        // ========================================
        "change_email.title" to "تغيير البريد الإلكتروني",
        "change_email.current_email" to "البريد الإلكتروني الحالي",
        "change_email.new_email_section" to "بريد إلكتروني جديد",
        "change_email.new_email" to "البريد الإلكتروني الجديد",
        "change_email.confirm_with_password" to "تأكيد بكلمة المرور",
        "change_email.save" to "حفظ",
        "change_email.saving" to "جاري الحفظ...",
        "change_email.all_fields_required" to "يرجى ملء جميع الحقول",
        "change_email.invalid_email" to "عنوان بريد إلكتروني غير صالح",
        "change_email.error" to "خطأ في تغيير البريد الإلكتروني",
        "change_email.info" to "سيتم استخدام بريدك الإلكتروني الجديد لتسجيل الدخول.",
        
        // ========================================
        // شاشة تغيير رقم الهاتف
        // ========================================
        "change_phone.title" to "تغيير رقم الهاتف",
        "change_phone.current_phone" to "رقم الهاتف الحالي",
        "change_phone.not_defined" to "غير محدد",
        "change_phone.new_phone_section" to "رقم جديد",
        "change_phone.new_phone" to "رقم الهاتف الجديد",
        "change_phone.phone_placeholder" to "+966 50 123 4567",
        "change_phone.leave_blank_to_remove" to "اتركه فارغاً للحذف",
        "change_phone.confirm_with_password" to "تأكيد بكلمة المرور",
        "change_phone.save" to "حفظ",
        "change_phone.saving" to "جاري الحفظ...",
        "change_phone.password_required" to "كلمة المرور مطلوبة",
        "change_phone.error" to "خطأ في تغيير رقم الهاتف",
        
        // ========================================
        // شاشة الأمان
        // ========================================
        "security.title" to "الأمان",
        "security.change_password" to "تغيير كلمة المرور",
        "security.current_password" to "كلمة المرور الحالية",
        "security.new_password" to "كلمة المرور الجديدة",
        "security.confirm_password" to "تأكيد كلمة المرور",
        "security.password_requirements" to "8 أحرف على الأقل",
        "security.change_password_button" to "تغيير كلمة المرور",
        "security.changing" to "جاري التغيير...",
        "security.all_fields_required" to "جميع الحقول مطلوبة",
        "security.passwords_do_not_match" to "كلمات المرور غير متطابقة",
        "security.password_too_short" to "يجب أن تحتوي كلمة المرور على 8 أحرف على الأقل",
        "security.change_password_error" to "خطأ في تغيير كلمة المرور",
        "security.password_changed" to "تم تغيير كلمة المرور بنجاح",
        "security.active_sessions" to "الجلسات النشطة",
        "security.no_active_sessions" to "لا توجد جلسات نشطة",
        "security.current_session" to "هذه الجلسة",
        "security.other_session" to "جهاز آخر",
        "security.current_badge" to "الحالي",
        "security.created_at" to "تم الإنشاء",
        "security.expires_at" to "ينتهي في",
        "security.revoke" to "إلغاء",
        "security.revoke_all" to "إلغاء الكل",
        "security.revoke_session_title" to "إلغاء الجلسة",
        "security.revoke_session_message" to "هل أنت متأكد من رغبتك في فصل هذا الجهاز؟",
        "security.revoke_all_sessions" to "فصل جميع الأجهزة",
        "security.revoke_all_sessions_title" to "فصل جميع الأجهزة",
        "security.revoke_all_sessions_message" to "هل أنت متأكد من رغبتك في فصل جميع الأجهزة الأخرى؟ ستحتاج إلى تسجيل الدخول مرة أخرى على كل منها.",
        "security.session_revoked" to "تم إلغاء الجلسة بنجاح",
        "security.all_sessions_revoked" to "تم إلغاء جميع الجلسات الأخرى",
        "security.revoke_session_error" to "خطأ في إلغاء الجلسة",
        "security.revoke_all_sessions_error" to "خطأ في إلغاء الجلسات",
        "security.security_tip" to "نصيحة: قم بتغيير كلمة المرور بانتظام وافصل الأجهزة التي لا تتعرف عليها.",
        
        // مؤشر قوة كلمة المرور
        "security.password_strength" to "قوة كلمة المرور",
        "security.password_weak" to "ضعيفة",
        "security.password_fair" to "مقبولة",
        "security.password_good" to "جيدة",
        "security.password_strong" to "قوية",
        
        // حالة الأمان
        "security.security_status" to "حالة الأمان",
        "security.account_protected" to "حسابك محمي",
        "security.active_devices" to "الأجهزة النشطة",
        "security.password_set" to "كلمة المرور مُحددة",
        
        // التحديثات في الوقت الفعلي
        "security.real_time_updates" to "تحديثات في الوقت الفعلي",
        
        // نصائح أمان محسّنة
        "security.security_tips_title" to "نصائح أمان",
        "security.tip_1" to "استخدم كلمة مرور فريدة من 12 حرفاً على الأقل تحتوي على أحرف كبيرة وصغيرة وأرقام ورموز.",
        "security.tip_2" to "لا تشارك كلمة المرور أبداً وفعّل المصادقة الثنائية إذا كانت متاحة.",
        "security.tip_3" to "تحقق بانتظام من جلساتك النشطة وافصل أي جهاز مشبوه.",
        
        // ========================================
        // حذف الحساب
        // ========================================
        "delete_account.title" to "حذف الحساب",
        "delete_account.warning" to "هذا الإجراء لا يمكن التراجع عنه. سيتم حذف جميع بياناتك نهائياً.",
        "delete_account.confirm_title" to "حذف حسابك؟",
        "delete_account.confirm_message" to "سيؤدي هذا الإجراء إلى حذف حسابك وجميع بياناتك نهائياً (الحجوزات، التقييمات، الصور، إلخ). هذا الإجراء لا يمكن التراجع عنه.",
        "delete_account.password_label" to "أدخل كلمة المرور للتأكيد",
        "delete_account.confirm_checkbox" to "أفهم أن هذا الإجراء لا يمكن التراجع عنه",
        "delete_account.delete_button" to "حذف حسابي",
        "delete_account.deleting" to "جاري الحذف...",
        "delete_account.success" to "تم حذف حسابك بنجاح",
        "delete_account.error" to "خطأ في حذف الحساب",
        
        // ========================================
        // شاشة طرق الدفع
        // ========================================
        "payment_methods.title" to "طرق الدفع",
        "payment_methods.no_cards" to "لا توجد بطاقات محفوظة",
        "payment_methods.description" to "سيتم حفظ بطاقات الدفع الخاصة بك بشكل آمن خلال عملية الشراء القادمة.",
        "payment_methods.stripe_info" to "معلومات الدفع الخاصة بك محمية بواسطة Stripe. نحن لا نخزن أرقام بطاقاتك أبداً.",
        "payment_methods.add_card" to "إضافة بطاقة",
        
        // ========================================
        // شاشة المستخدمين المحظورين
        // ========================================
        "blocked_users.title" to "المستخدمون المحظورون",
        "blocked_users.no_blocked_users" to "لا يوجد مستخدمون محظورون",
        "blocked_users.description" to "لن يتمكن المستخدمون الذين تحظرهم من رؤية ملفك الشخصي أو التواصل معك.",
        "blocked_users.unblock" to "إلغاء الحظر",
        
        // ========================================
        // مركز المساعدة
        // ========================================
        "help_center.title" to "مركز المساعدة",
        "help_center.faq_title" to "الأسئلة الشائعة",
        "help_center.need_more_help" to "لم تجد إجابة؟",
        "help_center.contact_support" to "اتصل بالدعم",
        "help_center.faq1_question" to "كيف ألغي حجزاً؟",
        "help_center.faq1_answer" to "اذهب إلى 'حجوزاتي'، اختر الحجز واضغط على 'إلغاء'. الإلغاء مجاني حتى 24 ساعة قبل الموعد.",
        "help_center.faq2_question" to "كيف أعدل ملفي الشخصي؟",
        "help_center.faq2_answer" to "اذهب إلى الإعدادات > حسابي > تعديل الملف الشخصي لتحديث معلوماتك الشخصية.",
        "help_center.faq3_question" to "كيف أتواصل مع صالون؟",
        "help_center.faq3_answer" to "في صفحة الصالون ستجد معلومات الاتصال: الهاتف، البريد الإلكتروني والعنوان.",
        "help_center.faq4_question" to "هل الدفع آمن؟",
        "help_center.faq4_answer" to "نعم، جميع المدفوعات تتم معالجتها عبر Stripe، الرائد العالمي في الدفع الإلكتروني. لا يتم تخزين معلوماتك المصرفية على خوادمنا أبداً.",
        
        // ========================================
        // الاتصال بالدعم
        // ========================================
        "contact.title" to "الاتصال بالدعم",
        "contact.response_time" to "نرد عادةً خلال 24-48 ساعة.",
        "contact.send_message" to "إرسال رسالة",
        "contact.subject" to "الموضوع",
        "contact.message" to "رسالتك",
        "contact.send" to "إرسال",
        "contact.sending" to "جاري الإرسال...",
        
        // ========================================
        // شروط الخدمة
        // ========================================
        "terms.title" to "شروط الخدمة",
        "terms.last_update" to "آخر تحديث: يناير 2025",
        "terms.section1_title" to "1. قبول الشروط",
        "terms.section1_content" to "باستخدام تطبيق Frollot، أنت توافق على شروط الخدمة هذه. إذا لم توافق، يرجى عدم استخدام التطبيق.",
        "terms.section2_title" to "2. وصف الخدمة",
        "terms.section2_content" to "Frollot هي منصة تربط صالونات الشعر بعملائها. تتيح الحجز الإلكتروني والدفع ومتابعة المواعيد.",
        "terms.section3_title" to "3. حساب المستخدم",
        "terms.section3_content" to "أنت مسؤول عن الحفاظ على سرية بيانات تسجيل الدخول الخاصة بك وجميع الأنشطة التي تتم تحت حسابك.",
        "terms.section4_title" to "4. المدفوعات والإلغاءات",
        "terms.section4_content" to "تتم معالجة المدفوعات بشكل آمن عبر Stripe. تختلف سياسات الإلغاء حسب كل صالون.",
        
        // ========================================
        // سياسة الخصوصية
        // ========================================
        "privacy.title" to "سياسة الخصوصية",
        "privacy.last_update" to "آخر تحديث: يناير 2025",
        "privacy.section1_title" to "1. البيانات المجمعة",
        "privacy.section1_content" to "نجمع البيانات التي تقدمها لنا (الاسم، البريد الإلكتروني، الهاتف) بالإضافة إلى بيانات استخدام التطبيق لتحسين خدماتنا.",
        "privacy.section2_title" to "2. استخدام البيانات",
        "privacy.section2_content" to "تُستخدم بياناتك لإدارة حجوزاتك وإرسال إشعارات مهمة وتحسين تجربة المستخدم.",
        "privacy.section3_title" to "3. مشاركة البيانات",
        "privacy.section3_content" to "نشارك معلومات حجزك مع الصالونات المعنية. نحن لا نبيع بياناتك الشخصية لأطراف ثالثة أبداً.",
        "privacy.section4_title" to "4. حقوقك",
        "privacy.section4_content" to "لديك الحق في الوصول إلى بياناتك الشخصية أو تعديلها أو حذفها في أي وقت من خلال إعدادات حسابك.",
        
        // ========================================
        // شاشة الدفع
        // ========================================
        "payment.title" to "الدفع",
        "payment.cancel" to "إلغاء",
        "payment.service" to "الخدمة:",
        "payment.salon" to "الصالون:",
        "payment.date" to "التاريخ:",
        
        // ========================================
        // شاشة طلب التحقق
        // ========================================
        "request_verification.title" to "طلب التحقق",
        "request_verification.header_title" to "طلب التحقق",
        "request_verification.description" to "اختر نوع التحقق الذي تريد الحصول عليه. سيراجع فريقنا طلبك قريباً.",
        "request_verification.verification_type" to "نوع التحقق *",
        "request_verification.additional_info" to "معلومات إضافية (اختياري)",
        "request_verification.additional_info_placeholder" to "صف وضعك، قدم المستندات (SIRET، الشهادات، إلخ)...",
        
        // ========================================
        // شاشة إنشاء تقييم
        // ========================================
        "create_review.title" to "ترك تقييم",
        "create_review.your_booking" to "حجزك",
        "create_review.salon" to "الصالون: {salonName}",
        "create_review.date" to "التاريخ: {date}",
        "create_review.rating_question" to "كيف تقيم تجربتك؟",
        "create_review.title_label" to "عنوان المراجعة (اختياري)",
        "create_review.title_placeholder" to "مثال: تجربة رائعة!",
        "create_review.comment_label" to "تعليقك (اختياري)",
        "create_review.comment_placeholder" to "شارك تجربتك...",
        
        // ========================================
        // شاشة إدارة مواعيد المالك
        // ========================================
        "owner_bookings_management.title" to "إدارة المواعيد",
        "owner_bookings_management.confirm" to "تأكيد",
        "owner_bookings_management.start" to "بدء",
        "owner_bookings_management.absent" to "غائب",
        "owner_bookings_management.finish" to "إنهاء",
        
        // ========================================
        // شاشة البحث
        // ========================================
        "search.title" to "البحث",
        "search.advanced_filters" to "مرشحات متقدمة",
        "search.post_type" to "نوع المنشور",
        "search.search_placeholder" to "ابحث عن منشورات، صالونات، مستخدمين...",
        "search.no_posts_found" to "لم يتم العثور على منشورات",
        "search.no_salons_found" to "لم يتم العثور على صالونات",
        "search.no_users_found" to "لم يتم العثور على مستخدمين",
        "search.no_hashtags_found" to "لم يتم العثور على علامات تصنيف",
        "search.no_results_found" to "لم يتم العثور على نتائج",
        "search.all" to "الكل",
        "search.load_more" to "تحميل المزيد",
        "search.posts" to "المنشورات ({count})",
        "search.salons" to "الصالونات ({count})",
        "search.users" to "المستخدمون ({count})",
        "search.hashtags" to "علامات التصنيف ({count})",
        
        // ========================================
        // مكون بطاقة المنشور المميزة جداً
        // ========================================
        "ultra_premium_post_card.oops" to "عذراً!",
        "ultra_premium_post_card.retry" to "إعادة المحاولة",
        "ultra_premium_post_card.no_posts_yet" to "لا توجد منشورات بعد",
        "ultra_premium_post_card.be_first_to_share" to "كن أول من يشارك\nشيئاً مذهلاً!",
        "ultra_premium_post_card.view_comments" to "عرض التعليق",
        "ultra_premium_post_card.view_comments_plural" to "عرض {count} تعليق",
        "ultra_premium_post_card.add_comment" to "أضف تعليقاً...",
        "ultra_premium_post_card.pinned" to "مثبت",
        
        // ========================================
        // الأزرار والإجراءات المشتركة
        // ========================================
        "common.cancel" to "إلغاء",
        "common.save" to "حفظ",
        "common.confirm" to "تأكيد",
        "common.retry" to "إعادة المحاولة",
        "common.delete" to "حذف",
        "common.edit" to "تعديل",
        "common.remove" to "إزالة",
        "common.add" to "إضافة",
        "common.create" to "إنشاء",
        "common.load_more" to "تحميل المزيد",
        "common.all" to "الكل",
        "common.start" to "بدء",
        "common.finish" to "إنهاء",
        "common.absent" to "غائب",
        "common.popular" to "شائع",
        "common.public" to "عام",
        "common.service" to "الخدمة:",
        "common.salon" to "الصالون:",
        "common.date" to "التاريخ:",
        "common.verified" to "متحقق",
        
        // ========================================
        // مكونات واجهة المستخدم
        // ========================================
        // RatingBar
        "components.rating_bar.star_filled" to "نجمة {number} ممتلئة",
        "components.rating_bar.star_empty" to "نجمة {number} فارغة",
        "components.rating_bar.reviews_count" to "({count} تقييم)",
        
        // PasswordTextField
        "components.password_text_field.label" to "كلمة المرور",
        "components.password_text_field.placeholder" to "أدخل كلمة المرور",
        "components.password_text_field.show_password" to "إظهار كلمة المرور",
        "components.password_text_field.hide_password" to "إخفاء كلمة المرور",
        
        // ReportDialog
        "components.report_dialog.title" to "الإبلاغ عن هذا {entity}",
        "components.report_dialog.info_message" to "سيتم مراجعة تقريرك من قبل فريق الإشراف لدينا. شكراً لمساعدتنا في الحفاظ على مجتمع محترم.",
        "components.report_dialog.reason_label" to "سبب الإبلاغ *",
        "components.report_dialog.additional_info_label" to "معلومات إضافية (اختياري)",
        "components.report_dialog.additional_info_placeholder" to "اوصف المشكلة باختصار...",
        "components.report_dialog.error_select_reason" to "يرجى اختيار سبب",
        "components.report_dialog.error_reporting" to "خطأ في الإبلاغ: {error}",
        "components.report_dialog.error_unknown" to "خطأ غير معروف",
        
        // UserAvatar
        "components.user_avatar.content_description" to "صورة رمزية لـ {name}",
        
        // QueueStatusCard
        "components.queue_status_card.connection_lost" to "فقدان الاتصال",
        "components.queue_status_card.data_stale" to "بيانات قديمة",
        "components.queue_status_card.last_update" to "آخر تحديث: منذ {minutes} دقيقة",
        "components.queue_status_card.your_progress" to "تقدمك",
        "components.queue_status_card.current_position" to "الموضع الحالي",
        "components.queue_status_card.estimated_time" to "الوقت المقدر",
        "components.queue_status_card.just_now" to "الآن",
        "components.queue_status_card.minutes_ago" to "منذ {minutes} دقيقة",
        "components.queue_status_card.leave_queue" to "مغادرة الطابور",
        "components.queue_status_card.reconnecting" to "إعادة الاتصال التلقائي قيد التقدم...",
        "components.queue_status_card.keep_app_open" to "احتفظ بالتطبيق مفتوحاً ليتم إشعارك من قبل الصالون.",
        "components.queue_status_card.status_offline" to "❌ غير متصل",
        "components.queue_status_card.status_pending" to "⏸️ تحديث معلق...",
        "components.queue_status_card.status_auto_refresh" to "✅ تحديث تلقائي (30 ثانية)",
        
        // FullScreenImageViewer
        "components.full_screen_image_viewer.image_content_description" to "صورة {number}",
        "components.full_screen_image_viewer.close" to "إغلاق",
        
        // AppDrawer
        "components.app_drawer.marketplace" to "السوق",
        "components.app_drawer.social" to "اجتماعي",
        "components.app_drawer.appointments" to "المواعيد",
        "components.app_drawer.account" to "الحساب",
        "components.app_drawer.profile" to "الملف الشخصي",
        "components.app_drawer.notifications" to "الإشعارات",
        "components.app_drawer.favorites" to "المفضلة",
        "components.app_drawer.archives" to "الأرشيف",
        "components.app_drawer.collections" to "المجموعات",
        "components.app_drawer.management" to "الإدارة",
        "components.app_drawer.my_salons" to "صالوناتي",
        "components.app_drawer.new_salon" to "صالون جديد",
        "components.app_drawer.create_post" to "إنشاء منشور",
        "components.app_drawer.bookings_management" to "إدارة المواعيد",
        "components.app_drawer.stats" to "الإحصائيات",
        "components.app_drawer.activity" to "النشاط",
        "components.app_drawer.my_portfolios" to "محافظي",
        "components.app_drawer.new_portfolio" to "محفظة جديدة",
        "components.app_drawer.services" to "الخدمات",
        "components.app_drawer.agenda" to "الأجندة",
        "components.app_drawer.admin" to "المسؤول",
        "components.app_drawer.dashboard" to "لوحة التحكم",
        "components.app_drawer.users" to "المستخدمون",
        "components.app_drawer.settings" to "الإعدادات",
        "components.app_drawer.help" to "المساعدة",
        "components.app_drawer.guest" to "ضيف",
        "components.app_drawer.client" to "عميل",
        "components.app_drawer.owner" to "مالك",
        "components.app_drawer.hairstylist" to "مصفف شعر",
        "components.app_drawer.admin_user" to "مسؤول",
        
        // ========================================
        // شاشة الدفع
        // ========================================
        "payment.title" to "الدفع",
        "payment.cancel" to "إلغاء",
        "payment.service" to "الخدمة",
        "payment.salon" to "الصالون",
        "payment.date" to "التاريخ",
        "payment.card_input" to "البطاقة",
        "payment.confirmation" to "التأكيد",
        "payment.processing" to "جاري المعالجة...",
        "payment.success" to "تم الدفع بنجاح",
        "payment.error" to "خطأ",
        "payment.continue" to "متابعة",
        "payment.retry" to "إعادة المحاولة",
        "payment.pay_amount" to "ادفع {amount}€",
        "payment.verify_order" to "تحقق من طلبك",
        "payment.booking_details" to "تفاصيل الحجز",
        "payment.card" to "البطاقة",
        "payment.total_to_pay" to "المبلغ الإجمالي",
        "payment.modify_card" to "تعديل البطاقة",
        "payment.processing_message" to "يرجى الانتظار بينما نعالج دفعتك بشكل آمن.",
        "payment.success_message" to "تم تأكيد حجزك. ستتلقى بريدًا إلكترونيًا للتأكيد.",
        "payment.error_message" to "حدث خطأ أثناء الدفع. يرجى المحاولة مرة أخرى.",
        "payment.view_booking" to "عرض حجزي",
        "payment.card_number" to "رقم البطاقة",
        "payment.card_holder" to "الاسم على البطاقة",
        "payment.expiry" to "تاريخ الانتهاء",
        "payment.cvv" to "CVV",
        "payment.ssl" to "SSL 256-bit",
        "payment.pci_dss" to "PCI-DSS",
        "payment.3d_secure" to "3D Secure",
        "payment.history" to "سجل المدفوعات",
        "payment.no_payments" to "لم تقم بأي دفعات بعد.",
        "payment.summary" to "ملخص",
        "payment.total_spent" to "إجمالي الإنفاق",
        "payment.transactions" to "المعاملات",
        "payment.success_rate" to "معدل النجاح",
        "payment.all_payments" to "الكل",
        "payment.succeeded" to "ناجحة",
        "payment.failed" to "فاشلة",
        "payment.refunded" to "مستردة",
        "payment.transaction_id" to "رقم المعاملة",
        "payment.stripe_reference" to "مرجع Stripe",
        "payment.payment_method" to "طريقة الدفع",
        "payment.currency" to "العملة",
        "payment.refunded_amount" to "المبلغ المسترد",
        
        // Stripe Checkout
        "payment.redirect" to "إعادة التوجيه",
        "payment.secure_payment" to "دفع آمن",
        "payment.secure_payment_description" to "سيتم توجيهك إلى صفحة الدفع الآمنة من Stripe.",
        "payment.order_summary" to "ملخص الطلب",
        "payment.total" to "المجموع",
        "payment.loading" to "جاري التحميل...",
        "payment.proceed_to_payment" to "المتابعة للدفع",
        "payment.secure_ssl" to "SSL 256-bit",
        "payment.stripe_secure" to "Stripe",
        "payment.pci_compliant" to "PCI-DSS",
        "payment.redirect_to_stripe" to "صفحة الدفع",
        "payment.redirect_description" to "أكمل الدفع على صفحة Stripe.\nعد هنا بعد الدفع.",
        "payment.open_payment_page" to "فتح صفحة الدفع",
        "payment.check_payment_status" to "التحقق من حالة الدفع",
        "payment.processing_payment" to "جاري المعالجة...",
        "payment.processing_description" to "يرجى الانتظار بينما نتحقق من الدفع الخاص بك.",
        "payment.payment_successful" to "تم الدفع بنجاح! 🎉",
        "payment.payment_success_description" to "تم تأكيد حجزك.\nستتلقى بريدًا إلكترونيًا للتأكيد.",
        "payment.payment_failed" to "فشل الدفع",
        "payment.payment_failed_description" to "حدث خطأ أثناء الدفع.\nيرجى المحاولة مرة أخرى.",
        "payment.step_summary" to "الملخص",
        "payment.step_payment" to "الدفع",
        "payment.step_confirmation" to "التأكيد",
        
        // ExternalShareDialog
        "components.external_share_dialog.title" to "مشاركة إلى",
        "components.external_share_dialog.share_via_app" to "مشاركة عبر تطبيق",
        "components.external_share_dialog.share_via_app_description" to "إنستغرام، واتساب، الرسائل، إلخ.",
        "components.external_share_dialog.error_sharing" to "خطأ في المشاركة: {error}",
        "components.external_share_dialog.copy_link" to "نسخ الرابط",
        "components.external_share_dialog.copy_link_description" to "نسخ رابط المنشور إلى الحافظة",
        "components.external_share_dialog.error_copying" to "خطأ في النسخ: {error}",
        "components.external_share_dialog.not_available" to "المشاركة الخارجية غير متاحة على هذه المنصة",
        
        // SearchTextField
        "components.search_text_field.placeholder" to "بحث...",
        "components.search_text_field.content_description" to "بحث",
        
        // ========================================
        // التعدادات - الترجمة
        // ========================================
        // BookingStatus
        "enums.booking_status.pending" to "قيد الانتظار",
        "enums.booking_status.confirmed" to "مؤكدة",
        "enums.booking_status.in_progress" to "قيد التنفيذ",
        "enums.booking_status.completed" to "مكتملة",
        "enums.booking_status.cancelled" to "ملغاة",
        "enums.booking_status.no_show" to "غياب",
        
        // PaymentStatus
        "enums.payment_status.pending" to "قيد الانتظار",
        "enums.payment_status.processing" to "قيد المعالجة",
        "enums.payment_status.succeeded" to "نجح",
        "enums.payment_status.failed" to "فشل",
        "enums.payment_status.canceled" to "ملغى",
        "enums.payment_status.partially_refunded" to "مسترد جزئياً",
        "enums.payment_status.unpaid" to "غير مدفوع",
        "enums.payment_status.paid" to "مدفوع",
        "enums.payment_status.refunded" to "مسترد",
        
        // PostType
        "enums.post_type.general" to "عام",
        "enums.post_type.avant_apres" to "قبل/بعد",
        "enums.post_type.portfolio" to "المحفظة",
        "enums.post_type.tendance" to "الاتجاه",
        "enums.post_type.conseil" to "نصيحة",
        "enums.post_type.realisation" to "إنجاز",
        "enums.post_type.inspiration" to "إلهام",
        "enums.post_type.general_description" to "منشور عام",
        "enums.post_type.avant_apres_description" to "إظهار تحول قبل/بعد",
        "enums.post_type.portfolio_description" to "إضافة إلى محفظتك",
        "enums.post_type.tendance_description" to "مشاركة اتجاه تصفيف الشعر",
        "enums.post_type.conseil_description" to "تقديم نصائح وحيل",
        "enums.post_type.realisation_description" to "إظهار إنجاز",
        "enums.post_type.inspiration_description" to "مشاركة إلهام",
        
        // PostVisibility
        "enums.post_visibility.public" to "عام",
        "enums.post_visibility.followers" to "المتابعون فقط",
        "enums.post_visibility.private" to "خاص",
        "enums.post_visibility.public_description" to "مرئي للجميع",
        "enums.post_visibility.followers_description" to "مرئي فقط لمتابعيك",
        "enums.post_visibility.private_description" to "مرئي لك فقط",
        
        // ServiceCategory
        "enums.service_category.coupe" to "قص وتصفيف",
        "enums.service_category.coloration" to "صبغة",
        "enums.service_category.soin" to "العناية",
        "enums.service_category.coiffage" to "التصفيف",
        "enums.service_category.barbe" to "الحلاق",
        "enums.service_category.technique" to "تقنيات خاصة",
        "enums.service_category.autre" to "خدمات أخرى",
        
        // ReactionType
        "enums.reaction_type.like" to "إعجاب",
        "enums.reaction_type.love" to "أحب",
        "enums.reaction_type.wow" to "واو",
        "enums.reaction_type.inspirant" to "ملهم",
        "enums.reaction_type.magnifique" to "رائع",
        "enums.reaction_type.bravo" to "برافو",
        "enums.reaction_type.like_description" to "إعجاب كلاسيكي",
        "enums.reaction_type.love_description" to "أحب هذا اللون!",
        "enums.reaction_type.wow_description" to "تحول لا يصدق!",
        "enums.reaction_type.inspirant_description" to "أريد نفس الشيء!",
        "enums.reaction_type.magnifique_description" to "عمل عالي الجودة!",
        "enums.reaction_type.bravo_description" to "تهانينا للمصفف!",
        
        // MediaType
        "enums.media_type.before" to "قبل",
        "enums.media_type.after" to "بعد",
        "enums.media_type.process" to "العملية",
        "enums.media_type.detail" to "التفاصيل",
        
        // ReportReason
        "enums.report_reason.inapproprie" to "محتوى غير مناسب",
        "enums.report_reason.spam" to "بريد إعلاني مزعج",
        "enums.report_reason.faux" to "قبل/بعد مزيف",
        "enums.report_reason.copyright" to "انتهاك حقوق النشر",
        "enums.report_reason.autre" to "آخر",
        "enums.report_reason.inapproprie_description" to "محتوى عنيف أو مضايق أو مسيء",
        "enums.report_reason.spam_description" to "إعلان غير مرغوب فيه أو محتوى متكرر",
        "enums.report_reason.faux_description" to "تحول أو نتيجة مضللة",
        "enums.report_reason.copyright_description" to "استخدام غير مصرح به لمحتوى محمي",
        "enums.report_reason.autre_description" to "سبب آخر يجب تحديده",
        
        // ReportedEntityType
        "enums.reported_entity_type.post" to "منشور",
        "enums.reported_entity_type.comment" to "تعليق",
        "enums.reported_entity_type.user" to "مستخدم",
        "enums.reported_entity_type.salon" to "صالون",
        
        // VerificationType
        "enums.verification_type.email" to "بريد إلكتروني متحقق",
        "enums.verification_type.phone" to "هاتف متحقق",
        "enums.verification_type.business" to "شركة متحققة",
        "enums.verification_type.professional" to "محترف متحقق",
        "enums.verification_type.email_description" to "بريد إلكتروني متحقق بالتأكيد",
        "enums.verification_type.phone_description" to "رقم هاتف متحقق",
        "enums.verification_type.business_description" to "شركة متحققة (سجل تجاري، مستندات)",
        "enums.verification_type.professional_description" to "شهادات ودبلومات متحققة",
        
        // BadgeCategory
        "enums.badge_category.certification" to "الشهادة",
        "enums.badge_category.competition" to "المسابقة",
        "enums.badge_category.formation" to "التدريب",
        "enums.badge_category.partenariat" to "الشراكة",
        
        // ReportStatus
        "enums.report_status.pending" to "قيد الانتظار",
        "enums.report_status.reviewed" to "قيد المراجعة",
        "enums.report_status.resolved" to "محلول",
        "enums.report_status.dismissed" to "مرفوض",
        
        // ModerationAction
        "enums.moderation_action.hide" to "إخفاء",
        "enums.moderation_action.delete" to "حذف",
        "enums.moderation_action.warn" to "تحذير",
        "enums.moderation_action.hide_description" to "سيتم إخفاء المحتوى عن جميع المستخدمين باستثناء المؤلف والمسؤولين.",
        "enums.moderation_action.delete_description" to "سيتم حذف المحتوى نهائياً ولا يمكن استعادته.",
        "enums.moderation_action.warn_description" to "سيتم إرسال تحذير إلى المؤلف دون تعديل المحتوى.",

        // AppealStatus
        "enums.appeal_status.none" to "لا استئناف",
        "enums.appeal_status.pending" to "قيد الانتظار",
        "enums.appeal_status.approved" to "مُعتمد",
        "enums.appeal_status.rejected" to "مرفوض",

        // ModerationAppealStatus
        "enums.appeal_status.none" to "لا يوجد استئناف",
        "enums.appeal_status.pending" to "قيد الانتظار",
        "enums.appeal_status.approved" to "موافق عليه",
        "enums.appeal_status.rejected" to "مرفوض",
        
        // HairHashtagCategory
        "enums.hair_hashtag_category.technique" to "التقنية",
        "enums.hair_hashtag_category.style" to "الأسلوب",
        "enums.hair_hashtag_category.couleur" to "اللون",
        "enums.hair_hashtag_category.longueur" to "الطول",
        "enums.hair_hashtag_category.texture" to "الملمس",
        
        // FollowingType
        "enums.following_type.coiffeur" to "مصفف شعر",
        "enums.following_type.salon" to "صالون",
    )
    
    return StringsBundle(strings)
}
