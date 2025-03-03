INSERT INTO public.categories ("name",macro_category,emoji,matchers,inserted_at,updated_at) VALUES
	 ('PALESTRA','subscriptions','🥊','{"(?i).*(palestra|asd molon labe sta).*"}','2025-02-18 21:13:40','2025-02-18 21:20:16'),
	 ('NETFLIX','subscriptions','🎬🍿','{(?i).*netflix.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('SPOTIFY','subscriptions','🟢ᯤ','{(?i).*spotify.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('CONTABO','subscriptions','💻','{(?i).*contabo.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('CHESS_DOT_COM','subscriptions','♟️','{(?i).*chess[._-]?dot[._-]?com.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('TELEFONO','subscriptions','📱','{(?i).*telefono.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('BOX','subscriptions','🅿️','{(?i).*box.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('KEEPER','subscriptions','🔐','{(?i).*keeper.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('ARUBA','subscriptions','🪪','{(?i).*aruba.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('CHAT_GPT','subscriptions','֎','{"(?i).*chat[ -]?gpt.*"}','2025-02-18 21:13:40','2025-02-18 21:13:40');
INSERT INTO public.categories ("name",macro_category,emoji,matchers,inserted_at,updated_at) VALUES
	 ('COPILOT','subscriptions','👨🏻‍💻','{(?i).*copilot.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('ICLOUD','subscriptions','🍎☁️','{(?i).*icloud.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('DISCOVERY','subscriptions','🎬 🔵','{(?i).*discovery.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('PRIME','subscriptions','🎬 🟢','{(?i).*prime.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('PREPLY','subscriptions','🇬🇧','{(?i).*preply.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('PARAMOUNT','subscriptions','⛰️','{(?i).*paramount.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('SUPERMERCATI','expense','🛒🥬🥒🍅','{"(?i).*(pam panorama|iper montebello).*"}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('DELIVERY','expense','🛵🍕','{(?i).*(deliveroo|delivery).*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('BENZINA','expense','⛽','{(?i).*benzina.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('AUTO','expense','🚗','{(?i).*auto.*}','2025-02-18 21:13:40','2025-02-18 21:13:40');
INSERT INTO public.categories ("name",macro_category,emoji,matchers,inserted_at,updated_at) VALUES
	 ('MOTO','expense','🏍️💨','{(?i).*moto.*}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('NON-CATEGORIZZATO','expense','❓','{}','2025-02-18 21:13:40','2025-02-18 21:13:40'),
	 ('SPESE MEDICHE','expense','💊','{(?i).*farmacia.*}','2025-02-18 21:13:41','2025-02-18 21:13:41'),
	 ('PULIZIE','expense','🧼🫧🧺🧽🧹','{(?i).*pulizie.*}','2025-02-18 21:13:41','2025-02-18 21:13:41'),
	 ('MONEYFARM','savings','💸🌽🧑‍🌾','{".*a favore mfm investment ltd.*"}','2025-02-18 20:46:05','2025-02-18 21:13:58'),
	 ('ELETTRICITA','bills','🔌','{"(?i).*a favore sorgenia.*"}','2025-02-18 21:13:40','2025-02-18 21:20:16'),
	 ('SPESE CONDOMINIALI','bills','🏢','{"(?i).*spese condominiali.*"}','2025-02-18 21:13:40','2025-02-18 21:20:16'),
	 ('INTERNET','bills','🌐','{"(?i).*a favore vodafone.*"}','2025-02-18 21:13:40','2025-02-18 21:20:16'),
	 ('AWS','bills','☁️','{(?i).*aws.*}','2025-02-18 21:13:40','2025-02-18 21:20:16'),
	 ('FONDO EMERGENZE','subscriptions','🚨','{"(?i).*fondo emergenze.*"}','2025-02-18 21:13:40','2025-03-03 08:28:49');
INSERT INTO public.categories ("name",macro_category,emoji,matchers,inserted_at,updated_at) VALUES
	 ('FONDO VACANZE','subscriptions','🏖️','{"(?i).*Sardegna SAVING_WITHDRAW.*"}','2025-02-18 21:13:40','2025-03-03 09:05:13'),
	 ('VACANZA','expense','🏖️','{(?i).*(vacanza).*}','2025-02-18 21:13:41','2025-03-03 09:05:30'),
	 ('FONDO SPESE CONDOMINIALI','subscriptions','🏢','{"(?i).*Spese condominiali porcodio SAVING_DEPOSIT.*"}','2025-03-02 18:56:43','2025-03-03 09:06:50'),
	 ('TRASPORTI','expense','🚉','{"(?i).*(taxi|trenord|trenitalia|italo treno).*","(?i).*Cless Ticket Atm Milan.*"}','2025-02-18 21:13:40','2025-03-03 09:13:55'),
	 ('TEMPO LIBERO','expense','🥃','{"(?i).*tempo libero.*",(?i).*P2P.*,"(?i).*Regalo Gabri SAVING_DEPOSIT.*"}','2025-02-18 21:13:40','2025-03-03 09:15:50');
