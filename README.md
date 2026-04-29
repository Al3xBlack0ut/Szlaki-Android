# Trails – Dokumentacja Techniczna Projektu
## **Autor:** Aleks Czarnecki
Aplikacja mobilna na platformę Android służąca do przeglądania tras turystycznych, monitorowania aktywności w czasie rzeczywistym oraz zarządzania danymi w modelu offline-first. Projekt realizuje założenia nowoczesnej architektury Android.


## Architektura i Stos Technologiczny

Aplikacja została zbudowana w oparciu o wzorzec **MVVM (Model-View-ViewModel)**, zapewniający separację logiki biznesowej od warstwy prezentacji.

*   **Język:** Kotlin
*   **UI:** Jetpack Compose (Material Design 3)
*   **Persystencja:** Room Persistence Library (SQLite)
*   **Komunikacja sieciowa:** Retrofit + OkHttp (REST API)
*   **Asynchroniczność:** Kotlin Coroutines & StateFlow
*   **Zarządzanie stanem:** ViewModel + SavedStateHandle
*   **Ładowanie obrazów:** Coil
*   **Serializacja:** Kotlinx Serialization

## Kluczowe Funkcjonalności

### 1. Zarządzanie Danymi (Offline-First)
Implementacja mechanizmu synchronizacji danych między zewnętrznym API a lokalną bazą danych.
*   Pobieranie danych za pomocą **Retrofit**.
*   Buforowanie (Cache) obiektów `Trail` w bazie **Room**, umożliwiające pracę bez dostępu do sieci.
*   Monitorowanie stanu synchronizacji i ładowania danych przez pasek postępu.

### 2. Zaawansowany System Monitorowania Aktywności
Komponent stopera zintegrowany z warstwą danych.
*   **Precyzja:** Odliczanie z dokładnością do 1 sekundy.
*   **Persystencja:** Automatyczny zapis wyniku (`TrailRecord`) do bazy danych po zatrzymaniu sesji.
*   **Logika biznesowa:** System `activeTrail` zapobiegający jednoczesnemu uruchomieniu stopera dla wielu tras.
*   **Global Timer Box:** Powiadomienie na ekranie głównym o trwającej aktywności.

### 3. Nawigacja i Interfejs Użytkownika
*   **Responsywność:** Dynamiczny układ `LazyVerticalGrid` (automatyczna zmiana liczby kolumn dla tabletów i orientacji poziomej).
*   **Navigation Drawer:** Szybki dostęp do kategorii głównych.
*   **HorizontalPager:** Obsługa gestów Swipe do nawigacji między podkategoriami (Piesze/Rowerowe).
*   **Wyszukiwarka:** Filtrowanie tras w czasie rzeczywistym z poziomu paska aplikacji (`TopAppBar`).

### 4. Animacje i UX
*   **Custom Splash Screen:** Animacja właściwości (`Animatable`) symulująca ruch fizyczny roweru (przesunięcie w osi X oraz efekt "bobbing" w osi Y).
*   **Material Design 3:** Pełna implementacja motywów Light/Dark, wykorzystanie Filter Chips do zaawansowanego filtrowania i Floating Action Button (FAB).
*   **System Ulubionych:** Mechanizm flagowania tras z trwałą synchronizacją stanu w bazie danych.

## Struktura Danych (Room)

*   `Trail`: Encja przechowująca metadane trasy (nazwa, opis, typ, parametry techniczne, status `isFavorite`).
*   `TrailRecord`: Encja przechowująca historię aktywności (relacja z trasą, timestamp, zmierzony czas).

## Instalacja i Wymagania

1.  Wymagane środowisko: **Android Studio Ladybug (2024.2.1)** lub nowsze.
2.  Minimalne SDK: **API 24** (Android 7.0).
3.  Target SDK: **API 35** (Android 15).
    
