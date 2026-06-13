# TikiTikiPhonk - Productivity App 📝⏱️🎵

**TikiTikiPhonk** adalah aplikasi produktivitas berbasis desktop yang menggabungkan manajemen To-Do List, Pomodoro Timer, dan pemutar *Ambient Audio* dalam satu antarmuka yang modern. Aplikasi ini dirancang untuk membantu pengguna tetap fokus dan melacak waktu yang mereka habiskan untuk merencanakan dan menyelesaikan berbagai tugas dengan menciptakan lingkungan suara yang mendukung konsentrasi tanpa perlu membuka aplikasi pihak ketiga.

Proyek ini dikembangkan oleh kelompok **PLR - Pemuda Legend Revolusioner** yang beranggotakan:
1. Riyan Ansari Harahap (241401003)
2. Christein Akadojuanrich Habayaki Purba (241401012)
3. El Fahreza Sufi (241401042)
4. Cristoval Pratama Siahaan (241401057)
5. M. Zidan Ruriano AG (241401063)

---

## 🌟 Fitur Utama

- **Manajemen Tugas Lengkap:** Tambah, edit, hapus, dan tandai tugas sebagai selesai. Dilengkapi dengan Kategori (Kerja, Fokus, Belajar, dll), Tenggat Waktu (Tanggal & Jam), dan Deskripsi detail.
- **Pomodoro Timer Terintegrasi:** Timer Pomodoro (25 menit fokus, 5 menit istirahat) yang bisa dihubungkan langsung dengan tugas tertentu. Waktu yang dihabiskan untuk suatu tugas akan tercatat secara otomatis!
- **Sistem Autentikasi (JWT):** Registrasi dan Login akun pengguna secara aman menggunakan JSON Web Tokens. Data tugas dan timer disimpan per-user.
- **Ambient Audio Player:** Pemutar audio latar bawaan (*White Noise, Rain, Forest, Brown Noise*) untuk membantu konsentrasi selama sesi fokus.
- **Statistik Sesi:** Melacak jumlah sesi fokus yang berhasil diselesaikan dan total menit fokus harian pengguna.
- **Dinamis & Responsif:** Tampilan Full-Screen otomatis saat masuk halaman utama, dilengkapi animasi yang *smooth* dan dukungan pergantian tema (*Light/Dark Mode*).

---

## ⚙️ Cara Menjalankan Aplikasi

Aplikasi ini dikompilasi menggunakan **Java Development Kit (JDK) versi 21**, namun dapat dijalankan dengan aman pada **JDK 22, 23, 24, 25, 26, maupun versi yang lebih baru** karena bahasa pemrograman Java bersifat *backward compatible*.

Disarankan untuk menjalankan aplikasi ini menggunakan IDE **IntelliJ IDEA**. Berikut adalah langkah-langkah instalasi dan eksekusinya:

### Langkah Instalasi (Persiapan Awal)
1. **Dapatkan *Source Code*:** Kode proyek ini bisa didapatkan dengan dua cara:
   - **Opsi A (Git Clone):** Buka terminal/Git Bash dan jalankan perintah:
     ```bash
     git clone https://github.com/LAVOTSIRC/UAS-PBO_TikiTikiPhonk_PLR.git
     ```
     atau
     ```bash
     git clone git@github.com:LAVOTSIRC/UAS-PBO_TikiTikiPhonk_PLR.git
     ```
   - **Opsi B (Download ZIP):** Jika tidak menggunakan Git, buka halaman GitHub proyek ini, klik tombol berwarna hijau **"<> Code"**, lalu pilih **"Download ZIP"**. Setelah terunduh, ekstrak file ZIP tersebut ke folder yang diinginkan.
2. **Buka Proyek:** Buka aplikasi IntelliJ IDEA, pilih menu **Open**, lalu arahkan ke folder proyek `UAS-PBO_TikiTikiPhonk_PLR` yang baru saja di-*clone*.
3. **Download Dependencies (Maven):** 
   - Proyek ini menggunakan **Maven** sebagai *build tool*. Saat proyek pertama kali dibuka, IntelliJ biasanya akan langsung mengenali file `pom.xml` dan mulai mengunduh semua *dependencies* (Spring Boot, JavaFX, H2, JJWT, dll) secara otomatis di latar belakang.
   - Jika tidak otomatis, buka panel **Maven** di sebelah kanan layar IntelliJ, lalu klik tombol **Reload All Maven Projects** (ikon panah melingkar).
   - Tunggu hingga proses *indexing* dan *download* selesai.

Setelah proses instalasi dan *dependencies* beres, aplikasi bisa dinyalakan menggunakan salah satu dari dua cara berikut:

### Cara 1: Menjalankan Menggunakan Script di IntelliJ IDEA
1. Buka folder proyek ini menggunakan IntelliJ IDEA.
2. Install plugin **Batch Script Support** melalui menu *Settings > Plugins* di IntelliJ jika belum terpasang.
3. Buka Menu *HamBurger* di ujung kiri atas workspace proyek.
4. Pilih menu **Run ➔ Edit Configurations...**.
5. Konfigurasi agar *Script* mengarah ke file `run-all.bat` tersebut.
6. Klik **Apply** lalu **OK**, setelah itu tekan tombol jalankan (**Run** ▶️) di tengah atas layar IntelliJ.
7. Script akan secara otomatis mem-*build* dan menyalakan Backend beserta Frontend secara bersamaan.

### Cara 2: Menjalankan Secara Manual (Run Class) di IntelliJ IDEA

**Langkah 1: Menjalankan Backend**
1. Buka Intellij, lalu buka panel *Open Project*.
2. Klik folder projek ini, lalu cari direktori `backend`, lalu *Select Folder* backend tersebut.
3. Setelah itu tekan tombol jalankan (**Run** ▶️) pada BackendApplication di tengah atas layar IntelliJ.
4. Tunggu hingga di *Run Console* bawah muncul log `Started BackendApplication`.

**Langkah 2: Menjalankan Frontend**
1. Pastikan Backend sudah dalam kondisi menyala.
2. Buka Intellij di window baru, lalu buka panel *Open Project*.
3. Klik folder projek ini, lalu cari direktori `frontend`, lalu *Select Folder* frontend tersebut.
4. Setelah itu tekan tombol jalankan (**Run** ▶️) pada Main di tengah atas layar IntelliJ.
5. Jendela aplikasi TikiTikiPhonk akan otomatis terbuka.

---

## 🎥 Video Presentasi

Penjelasan lebih lanjut mengenai alur kerja dan demonstrasi aplikasi TikiTikiPhonk dapat dilihat pada video YouTube berikut:
https://youtu.be/UQ32RS8JYck?si=68GmkwXsXU1VDUCU
