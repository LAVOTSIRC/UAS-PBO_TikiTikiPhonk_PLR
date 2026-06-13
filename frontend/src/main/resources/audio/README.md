# Audio Resource Files

Folder ini berisi file audio bawaan untuk fitur Noise Tab.

## File yang Dibutuhkan

Letakkan file-file berikut di folder ini:

| Filename          | Keterangan                      |
|-------------------|---------------------------------|
| `white_noise.mp3` | White noise (suara statis putih) |
| `brown_noise.mp3` | Brown noise (suara rendah dalam) |
| `rain.mp3`        | Suara hujan                      |
| `forest.mp3`      | Suara hutan / alam               |

## Format yang Didukung

- MP3 (`.mp3`) — direkomendasikan
- WAV (`.wav`) — didukung tapi ukuran file lebih besar

## Cara Mendapatkan File Audio

Anda dapat mengunduh file audio bebas royalti dari:

- [Freesound.org](https://freesound.org) — search "white noise loop", "rain ambience loop", dll.
- [Pixabay](https://pixabay.com/sound-effects/) — ambient & nature sounds
- [Zapsplat](https://www.zapsplat.com) — gratis dengan akun

## Cara Memuat dari Code (referensi)

```java
// Di AudioPlayerService.java
URL resourceUrl = getClass().getClassLoader()
    .getResource("audio/white_noise.mp3");

if (resourceUrl != null) {
    String uri = resourceUrl.toExternalForm();
    // uri siap dipakai sebagai parameter Media(uri)
}
```

> **Catatan:** Jika file tidak ditemukan saat runtime, label "Now Playing" 
> akan menampilkan pesan error: `⚠ File tidak ditemukan: audio/white_noise.mp3`
> Aplikasi tidak akan crash.
