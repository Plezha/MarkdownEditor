## Обратная связь
В любом случае буду рад получить обратную связь по поводу этого приложения. Для этого можно открыть issue или написать мне в [телеграм](https://t.me/plezhaa)

## Что из себя представляет это приложение

### Экран загрузки документа

Пользователь может:
 - Выбрать **локальный файл** (.md) с устройства.
 - Ввести **URL-файл** (по http/https) и скачать его.
 - Загруженный текст отображается в preview-режиме или редактируется по выбору.

### Экран просмотра Markdown
 - Отображает отформатированный документ.
 - Отображение осуществляется через **View-компоненты**.
 - Все элементы Markdown отображаются "нативно", являясь Android View внутри RecyclerView.
<img src="https://github.com/user-attachments/assets/9da7415e-91e3-404d-a994-faee66b15ea1" alt="drawing" width="300"/>


### Экран редактирования
- Текстовое поле с возможностью редактирования содержимого Markdown.
- Кнопка "Сохранить" возвращает к просмотру.
<img src="https://github.com/user-attachments/assets/211f9b27-5e91-46a3-a13e-0c77f3d08790" alt="drawing" width="300"/>


### Markdown-поддержка

1. Заголовки уровней 1-6.
2. Жирный текст.
3. Курсивный текст.
4. Зачеркнутый текст.
5. Таблицы (пример подходящей таблицы можно увидеть в Unit-тесте [`SimpleMarkdownParserUnitTest.kt`](https://github.com/Plezha/MarkdownEditor/blob/main/app/src/test/java/com/plezha/markdowneditor/SimpleMarkdownParserUnitTest.kt)).
6. Изображения (по ссылке).

### Остальное

1. Используемые библиотеки:
  - `androidx.activity`
  - `androidx.appcompat`
  - `androidx.constraintlayout`
  - `com.google.android.material`
  - И не более
2. Не используется WebView.
3. Покрытие логики парсинга юнит-тестами [`SimpleMarkdownParserUnitTest.kt`](https://github.com/Plezha/MarkdownEditor/blob/main/app/src/test/java/com/plezha/markdowneditor/SimpleMarkdownParserUnitTest.kt).
4. Кэш изображений в памяти устройства с помощью [`ImageCache.kt`](https://github.com/Plezha/MarkdownEditor/blob/main/app/src/main/java/com/plezha/markdowneditor/ImageCache.kt) - как постоянной, так и оперативной.
5. Обработка ошибок.
6. Оформленный README с кратким описанием решения.
