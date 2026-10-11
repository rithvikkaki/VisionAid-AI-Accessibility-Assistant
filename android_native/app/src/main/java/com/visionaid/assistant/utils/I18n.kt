package com.visionaid.assistant.utils

/**
 * Internationalization utility for VisionAid.
 * Provides localized labels for all 15 supported languages.
 */
object I18n {
    private val labels: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            "nav_scan" to "Scan", "nav_history" to "History", "nav_settings" to "Settings", "nav_back" to "Back",
            "btn_speak" to "Speak", "btn_capture" to "Capture", "btn_ask_question" to "Ask a Question",
            "btn_change" to "Change", "btn_reset" to "Reset All Settings", "swipe_menu" to "Swipe up for menu",
            "text_processing" to "Processing...", "text_listening" to "Listening...",
            "label_lang" to "Language", "label_reading_level" to "Reading Level", "label_voice_speed" to "Voice Speed",
            "label_interface" to "Interface", "label_dark_mode" to "Dark Mode", "label_high_contrast" to "High contrast",
            "rl_simple" to "Simple", "rl_medium" to "Medium", "rl_detailed" to "Detailed",
            "player_ready" to "Ready to Scan", "error_connection" to "Check connection to server.", 
            "error_server" to "Server error.", "error_no_speech" to "I didn't hear anything.",
            "voice_mode_started" to "Voice mode started", "capture_mode_started" to "Capture mode started", 
            "lang_changed" to "Language changed to English", "label_selected" to "selected"
        ),
        "te" to mapOf(
            "nav_scan" to "స్కాన్", "nav_history" to "చరిత్ర", "nav_settings" to "అమరికలు", "nav_back" to "వెనుకకు",
            "btn_speak" to "మాట్లాడండి", "btn_capture" to "క్యాప్చర్", "btn_ask_question" to "ప్రశ్న అడగండి",
            "btn_change" to "మార్చు", "btn_reset" to "రీసెట్", "swipe_menu" to "మెను కోసం పైకి స్వైప్ చేయండి",
            "text_processing" to "ప్రాసెసింగ్...", "text_listening" to "వింటున్నాను...",
            "label_lang" to "భాష", "label_reading_level" to "చదివే స్థాయి", "label_voice_speed" to "వాయిస్ వేగం",
            "label_interface" to "ఇంటర్ఫేస్", "label_dark_mode" to "డార్క్ మోడ్", "label_high_contrast" to "అధిక కాంట్రాస్ట్",
            "rl_simple" to "సులభం", "rl_medium" to "మధ్యస్థం", "rl_detailed" to "వివరణాత్మకం",
            "player_ready" to "సిద్ధంగా ఉంది", "error_connection" to "కనెక్షన్‌ని తనిఖీ చేయండి.",
            "voice_mode_started" to "వాయిస్ మోడ్ ప్రారంభమైంది", "capture_mode_started" to "క్యాప్చర్ మోడ్ ప్రారంభమైంది", 
            "lang_changed" to "భాష తెలుగులోకి మార్చబడింది", "label_selected" to "ఎంచుకోబడింది"
        ),
        "hi" to mapOf(
            "nav_scan" to "स्कैन", "nav_history" to "इतिहास", "nav_settings" to "सेटिंग्स", "nav_back" to "पीछे",
            "btn_speak" to "बोलें", "btn_capture" to "कैप्चर", "btn_ask_question" to "सवाल पूछें",
            "btn_change" to "बदलें", "btn_reset" to "रीसेट करें", "swipe_menu" to "मेनु के लिए ऊपर स्वाइप करें",
            "text_processing" to "प्रसंस्करण...", "text_listening" to "सुन रहा हूँ...",
            "label_lang" to "भाषा", "label_reading_level" to "पढ़ने का स्तर", "label_voice_speed" to "आवाज की गति",
            "label_interface" to "इंटरफेस", "label_dark_mode" to "डार्क मोड", "label_high_contrast" to "उच्च कंट्रास्ट",
            "rl_simple" to "साधारण", "rl_medium" to "मध्यम", "rl_detailed" to "विस्तृत",
            "player_ready" to "तैयार है", "error_connection" to "कनेक्शन की जांच करें।",
            "voice_mode_started" to "वॉयस मोड शुरू हुआ", "capture_mode_started" to "कैप्चर मोड शुरू हुआ", 
            "lang_changed" to "भाषा हिंदी में बदल दी गई", "label_selected" to "चुना गया"
        ),
        "ta" to mapOf(
            "nav_scan" to "ஸ்கேன்", "nav_history" to "வரலாறு", "nav_settings" to "அமைப்புகள்", "nav_back" to "பின்னால்",
            "btn_speak" to "பேசவும்", "btn_capture" to "பிடிப்பு", "btn_ask_question" to "கேள்வி கேட்க",
            "btn_change" to "மாற்று", "btn_reset" to "மீட்டமை", "swipe_menu" to "மேலே ஸ்வைப் செய்யவும்",
            "text_processing" to "செயலாக்கம்...", "text_listening" to "கேட்கிறது...",
            "label_lang" to "மொழி", "label_reading_level" to "வாசிப்பு நிலை", "label_voice_speed" to "குரல் வேகம்",
            "label_interface" to "இடைமுகம்", "label_dark_mode" to "இருண்ட பயன்முறை", "label_high_contrast" to "அதிக மாறுபாடு",
            "rl_simple" to "எளிய", "rl_medium" to "நடுத்தர", "rl_detailed" to "விரிவான",
            "player_ready" to "தயார்", "error_connection" to "இணைப்பைச் சரிபார்க்கவும்.",
            "voice_mode_started" to "குரல் பயன்முறை தொடங்கியது", "capture_mode_started" to "பிடிப்பு பயன்முறை தொடங்கியது", 
            "lang_changed" to "மொழி தமிழுக்கு மாற்றப்பட்டது", "label_selected" to "தேர்ந்தெடுக்கப்பட்டது"
        ),
        "kn" to mapOf(
            "nav_scan" to "ಸ್ಕ್ಯಾನ್", "nav_history" to "ಇತಿಹಾಸ", "nav_settings" to "ಸೆಟ್ಟಿಂಗ್‌ಗಳು", "nav_back" to "ಹಿಂದಕ್ಕೆ",
            "btn_speak" to "ಮಾತನಾಡಿ", "btn_capture" to "ಸೆರೆಹಿಡಿಯಿರಿ", "btn_ask_question" to "ಪ್ರಶ್ನೆ ಕೇಳಿ",
            "btn_change" to "ಬದಲಾಯಿಸಿ", "btn_reset" to "ಮರುಹೊಂದಿಸಿ", "swipe_menu" to "ಮೇಲಕ್ಕೆ స్వైప్ మాడి",
            "text_processing" to "సంస్కరణాగుత్తిదే...", "text_listening" to "కేళిసికొళ్లుత్తిదే...",
            "label_lang" to "భాష", "label_reading_level" to "ఓదుವ ಮಟ್ಟ", "label_voice_speed" to "ధ్వನಿ వేగ",
            "label_interface" to "ఇంటర్ఫేస్", "label_dark_mode" to "డార్క్ మోడ్", "label_high_contrast" to "ಹೆಚ್ಚಿನ ಕಾಂಟ್ರಾಸ್ಟ್",
            "rl_simple" to "సరళ", "rl_medium" to "ಮధ్యಮ", "rl_detailed" to "ವಿವರಣಾತ್ಮಕ",
            "player_ready" to "ಸಿದ್ಧವಾಗಿದೆ", "error_connection" to "సంపర్కవన్ను పరిక్షీసి.",
            "voice_mode_started" to "ధ్వನಿ ಮೋಡ್ ಪ್ರಾರಂಭವಾಯಿತು", "capture_mode_started" to "ಸೆರೆಹಿಡಿಯುವ ಮೋಡ್ ಪ್ರಾರಂಭವಾಯಿತು", 
            "lang_changed" to "భాషయన్ను కన్నడక్కే బదలాయిసలాగిదే", "label_selected" to "ಆಯ್ಕೆ ಮಾಡಲಾಗಿದೆ"
        ),
        "ml" to mapOf(
            "nav_scan" to "സ്കാൻ", "nav_history" to "ചരിത്രം", "nav_settings" to "ക്രമീകരണങ്ങൾ", "nav_back" to "പുറകോട്ട്",
            "btn_speak" to "സംസാരിക്കുക", "btn_capture" to "ക്യാപ്ചർ", "btn_ask_question" to "ചോദ്യം ചോദിക്കുക",
            "btn_change" to "മാറ്റുക", "btn_reset" to "റീസെറ്റ്", "swipe_menu" to "മുകളിലേക്ക് സ്വൈപ്പ് ചെയ്യുക",
            "text_processing" to "പ്രോസസ്സിംഗ്...", "text_listening" to "ശ്രദ്ധിക്കുന്നു...",
            "label_lang" to "ഭാഷ", "label_reading_level" to "വായനാ നില", "label_voice_speed" to "ശബ്ദ വേഗത",
            "label_interface" to "ഇന്റർഫേസ്", "label_dark_mode" to "ഡാർക്ക് മോഡ്", "label_high_contrast" to "ഹൈ കോൺട്രാസ്റ്റ്",
            "rl_simple" to "ലളിതം", "rl_medium" to "ഇടത്തരം", "rl_detailed" to "വിശദമായ",
            "player_ready" to "തയ്യാറാണ്", "error_connection" to "കണക്ഷൻ പരിശോധിക്കുക.",
            "voice_mode_started" to "വോയ്‌സ് മോഡ് ആരംഭിച്ചു", "capture_mode_started" to "ക്യാപ്‌ചർ മോഡ് ആരംഭിച്ചു", 
            "lang_changed" to "ഭാഷ മലയാളത്തിലേക്ക് മാറ്റി", "label_selected" to "തിരഞ്ഞെടുത്തു"
        ),
        "mr" to mapOf(
            "nav_scan" to "स्कॅन", "nav_history" to "इतिहास", "nav_settings" to "सेटिंग्ज", "nav_back" to "मागे",
            "btn_speak" to "बोला", "btn_capture" to "कॅप्चर", "btn_ask_question" to "प्रश्न विचारा",
            "btn_change" to "बदला", "btn_reset" to "रीसेट", "swipe_menu" to "वर स्वाइप करा",
            "text_processing" to "प्रक्रिया होत आहे...", "text_listening" to "ऐकत आहे...",
            "label_lang" to "भाषा", "label_reading_level" to "वाचन स्तर", "label_voice_speed" to "आवाजाचा वेग",
            "label_interface" to "इंटरफेस", "label_dark_mode" to "डार्क मोड", "label_high_contrast" to "हाय कॉन्ट्रास्ट",
            "rl_simple" to "सोपे", "rl_medium" to "मध्यम", "rl_detailed" to "तपशीलवार",
            "player_ready" to "तयार आहे", "error_connection" to "कनेक्शन तपासा.",
            "voice_mode_started" to "व्हॉइस मोड सुरू झाला", "capture_mode_started" to "कॅप्चर मोड सुरू झाला", 
            "lang_changed" to "भाषा मराठीत बदलली", "label_selected" to "निवडले"
        ),
        "bn" to mapOf(
            "nav_scan" to "স্ক্যান", "nav_history" to "ইতিহাস", "nav_settings" to "সেটিংস", "nav_back" to "পিছনে",
            "btn_speak" to "বলুন", "btn_capture" to "ক্যাপচার", "btn_ask_question" to "প্রশ্ন জিজ্ঞাসা করুন",
            "btn_change" to "পরিবর্তন", "btn_reset" to "রিসেট", "swipe_menu" to "উপরে সোয়াইপ করুন",
            "text_processing" to "প্রক্রিয়াকরণ...", "text_listening" to "শুনছি...",
            "label_lang" to "ভাষা", "label_reading_level" to "পড়ার স্তর", "label_voice_speed" to "ভয়েস গতি",
            "label_interface" to "ইন্টারফেস", "label_dark_mode" to "ডার্ক মোড", "label_high_contrast" to "হাই কন্ট্রাস্ট",
            "rl_simple" to "সহজ", "rl_medium" to "মাঝারি", "rl_detailed" to "বিস্তারিত",
            "player_ready" to "প্রস্তুত", "error_connection" to "সংযোগ পরীক্ষা করুন।",
            "voice_mode_started" to "ভয়েস মোড শুরু হয়েছে", "capture_mode_started" to "ক্যাপচার মোড শুরু হয়েছে", 
            "lang_changed" to "ভাষা বাংলায় পরিবর্তন করা হয়েছে", "label_selected" to "নির্বাচিত"
        ),
        "gu" to mapOf(
            "nav_scan" to "સ્કેન", "nav_history" to "ઇતિહાસ", "nav_settings" to "સેટિંગ્સ", "nav_back" to "પાછળ",
            "btn_speak" to "બોલો", "btn_capture" to "કેપ્ચર", "btn_ask_question" to "પ્રશ્ન પૂછો",
            "btn_change" to "બદલો", "btn_reset" to "રીસેટ", "swipe_menu" to "ઉપર સ્વાઇપ કરો",
            "text_processing" to "પ્રોસેસિંગ...", "text_listening" to "સાંભળી રહ્યા છીએ...",
            "label_lang" to "ભાષા", "label_reading_level" to "વાંચન સ્તર", "label_voice_speed" to "અવાજની ગતિ",
            "label_interface" to "ઇન્ટરફેસ", "label_dark_mode" to "ડાર્ક મોડ", "label_high_contrast" to "હાઈ કોન્ટ્રાસ્ટ",
            "rl_simple" to "સરળ", "rl_medium" to "મધ્યમ", "rl_detailed" to "વિગતવાર",
            "player_ready" to "તૈયાર", "error_connection" to "કનેક્શન તપાસો.",
            "voice_mode_started" to "વોઈસ મોડ શરૂ થયો", "capture_mode_started" to "કેપ્ચર મોડ શરૂ થયો", 
            "lang_changed" to "ભાષા ગુજરાતીમાં બદલાઈ", "label_selected" to "પસંદ કરેલ"
        ),
        "pa" to mapOf(
            "nav_scan" to "ਸਕੈਨ", "nav_history" to "ਇਤਿਹਾਸ", "nav_settings" to "ਸੈਟਿੰਗਾਂ", "nav_back" to "ਪਿੱਛੇ",
            "btn_speak" to "ਬੋਲੋ", "btn_capture" to "ਕੈਪਚਰ", "btn_ask_question" to "ਸਵਾਲ ਪੁੱਛੋ",
            "btn_change" to "ਬਦਲੋ", "btn_reset" to "ਰੀਸੈਟ", "swipe_menu" to "ਉੱਪਰ ਸਵਾਈਪ ਕਰੋ",
            "text_processing" to "ਪ੍ਰੋਸੈਸਿੰਗ...", "text_listening" to "ਸੁਣ ਰਿਹਾ ਹੈ...",
            "label_lang" to "ਭਾਸ਼ਾ", "label_reading_level" to "ਪੜ੍ਹਨ ਦਾ ਪੱਧਰ", "label_voice_speed" to "ਆਵਾਜ਼ ਦੀ ਗਤੀ",
            "label_interface" to "ਇੰਟਰਫੇਸ", "label_dark_mode" to "ਡਾਰਕ ਮੋਡ", "label_high_contrast" to "ਹਾਈ ਕੰਟਰਾਸਟ",
            "rl_simple" to "ਸਧਾਰਨ", "rl_medium" to "ਦਰਮਿਆਨਾ", "rl_detailed" to "ਵਿਸਤ੍ਰਿਤ",
            "player_ready" to "ਤਿਆਰ", "error_connection" to "ਕਨੈਕਸ਼ਨ ਦੀ ਜਾਂਚ ਕਰੋ।",
            "voice_mode_started" to "ਵੌਇਸ ਮੋਡ ਸ਼ੁਰੂ ਹੋਇਆ", "capture_mode_started" to "ਕੈਪਚਰ ਮੋਡ ਸ਼ੁਰੂ ਹੋਇਆ", 
            "lang_changed" to "ਭਾਸ਼ਾ ਪੰਜਾਬੀ ਵਿੱਚ ਬਦਲ ਗਈ", "label_selected" to "ਚੁਣਿਆ ਗਿਆ"
        ),
        "ur" to mapOf(
            "nav_scan" to "اسکین", "nav_history" to "تاریخ", "nav_settings" to "ترتیبات", "nav_back" to "پیچھے",
            "btn_speak" to "بولیں", "btn_capture" to "کیپچر", "btn_ask_question" to "سوال پوچھیں",
            "btn_change" to "تبدیل کریں", "btn_reset" to "ری سیٹ", "swipe_menu" to "اوپر سوائپ کریں",
            "text_processing" to "پروسیسنگ ہو رہی ہے...", "text_listening" to "سن رہا ہوں...",
            "label_lang" to "زبان", "label_reading_level" to "پڑھنے کی سطح", "label_voice_speed" to "آواز کی رفتار",
            "label_interface" to "انٹرفیس", "label_dark_mode" to "ڈارک موریڈ", "label_high_contrast" to "ہائی کنٹراسٹ",
            "rl_simple" to "سادہ", "rl_medium" to "درمیانہ", "rl_detailed" to "تفصیلی",
            "player_ready" to "تیار ہے", "error_connection" to "کنکشن چیک کریں۔",
            "voice_mode_started" to "وائس موڈ شروع ہو گیا", "capture_mode_started" to "کیپچر موڈ شروع ہو گیا", 
            "lang_changed" to "زبان اردو میں تبدیل کر دی گئی", "label_selected" to "منتخب کیا گیا"
        ),
        "fr" to mapOf(
            "nav_scan" to "Scanner", "nav_history" to "Historique", "nav_settings" to "Paramètres", "nav_back" to "Retour",
            "btn_speak" to "Parler", "btn_capture" to "Capturer", "btn_ask_question" to "Poser une question",
            "btn_change" to "Modifier", "btn_reset" to "Réinitialiser", "swipe_menu" to "Balayer vers le haut",
            "text_processing" to "Traitement...", "text_listening" to "Écoute...",
            "label_lang" to "Langue", "label_reading_level" to "Niveau de lecture", "label_voice_speed" to "Vitesse",
            "label_interface" to "Interface", "label_dark_mode" to "Mode sombre", "label_high_contrast" to "Contraste élevé",
            "rl_simple" to "Simple", "rl_medium" to "Moyen", "rl_detailed" to "Détaillé",
            "player_ready" to "Prêt", "error_connection" to "Vérifiez la connexion.",
            "voice_mode_started" to "Mode voix démarré", "capture_mode_started" to "Mode capture démarré", 
            "lang_changed" to "Langue changée en français", "label_selected" to "sélectionné"
        ),
        "es" to mapOf(
            "nav_scan" to "Escanear", "nav_history" to "Historial", "nav_settings" to "Ajustes", "nav_back" to "Atrás",
            "btn_speak" to "Hablar", "btn_capture" to "Capturar", "btn_ask_question" to "Hacer una pregunta",
            "btn_change" to "Cambiar", "btn_reset" to "Restablecer", "swipe_menu" to "Deslizar arriba",
            "text_processing" to "Procesando...", "text_listening" to "Escuchando...",
            "label_lang" to "Idioma", "label_reading_level" to "Nivel de lectura", "label_voice_speed" to "Velocidad",
            "label_interface" to "Interfaz", "label_dark_mode" to "Modo oscuro", "label_high_contrast" to "Alto contraste",
            "rl_simple" to "Simple", "rl_medium" to "Medio", "rl_detailed" to "Detallado",
            "player_ready" to "Listo", "error_connection" to "Compruebe la conexión.",
            "voice_mode_started" to "Modo de voz iniciado", "capture_mode_started" to "Modo de captura iniciado", 
            "lang_changed" to "Idioma cambiado a español", "label_selected" to "seleccionado"
        ),
        "de" to mapOf(
            "nav_scan" to "Scannen", "nav_history" to "Verlauf", "nav_settings" to "Einstellungen", "nav_back" to "Zurück",
            "btn_speak" to "Sprechen", "btn_capture" to "Erफassen", "btn_ask_question" to "Frage stellen",
            "btn_change" to "Ändern", "btn_reset" to "Zurücksetzen", "swipe_menu" to "Nach oben wischen",
            "text_processing" to "Verarbeitung...", "text_listening" to "Hören...",
            "label_lang" to "Sprache", "label_reading_level" to "Lesestufe", "label_voice_speed" to "Geschwindigkeit",
            "label_interface" to "Oberfläche", "label_dark_mode" to "Dunkelmodus", "label_high_contrast" to "Hoher Kontrast",
            "rl_simple" to "Einfach", "rl_medium" to "Mittel", "rl_detailed" to "Detailliert",
            "player_ready" to "Bereit", "error_connection" to "Verbindung prüfen.",
            "voice_mode_started" to "Sprachmodus gestartet", "capture_mode_started" to "Aufnahmemodus gestartet", 
            "lang_changed" to "Sprache auf Deutsch umgestellt", "label_selected" to "ausgewählt"
        ),
        "ja" to mapOf(
            "nav_scan" to "スキャン", "nav_history" to "履歴", "nav_settings" to "設定", "nav_back" to "戻る",
            "btn_speak" to "話す", "btn_capture" to "撮影", "btn_ask_question" to "質問する",
            "btn_change" to "変更", "btn_reset" to "リセット", "swipe_menu" to "上にスワイプ",
            "text_processing" to "処理中...", "text_listening" to "聴取中...",
            "label_lang" to "言語", "label_reading_level" to "読解レベル", "label_voice_speed" to "再生速度",
            "label_interface" to "インターフェース", "label_dark_mode" to "ダークモード", "label_high_contrast" to "高コントラスト",
            "rl_simple" to "シンプル", "rl_medium" to "標準", "rl_detailed" to "詳細",
            "player_ready" to "準備完了", "error_connection" to "接続を確認してください。",
            "voice_mode_started" to "音声モードが開始されました", "capture_mode_started" to "撮影モードが開始されました", 
            "lang_changed" to "言語が日本語に変更されました", "label_selected" to "選択されました"
        )
    )

    /**
     * Get a localized label. Falls back to English if key is missing for the language.
     */
    fun get(lang: String, key: String): String {
        return labels[lang]?.get(key) ?: labels["en"]?.get(key) ?: key
    }
}
