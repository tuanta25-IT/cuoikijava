# Library Management Desktop (Swing)

Du an desktop Java Swing duoc tach rieng tu he thong web Spring Boot, dung chung database `QuanLyThuVien`.

## Tinh nang da co

- Dang nhap bang `Username` hoac `Email` (xac thuc bcrypt tu bang `Users`).
- Giao dien sang, mau sac, role-based tabs/controls theo tai khoan.
- Dashboard thong ke nhanh:
  - Tong so sach
  - Tong doc gia
  - So phieu muon dang muon
  - So phieu muon qua han
  - So luot dat truoc con hieu luc
- CRUD The loai
- CRUD Sach
- CRUD Doc gia
- Tao phieu muon, xem chi tiet phieu muon, them/xoa sach trong phieu, danh dau da tra va gia han
- Dat truoc sach va huy dat truoc
- Xuat Excel/PDF cho cac bang du lieu va bao cao tong quan

## Cau hinh ket noi DB

File cau hinh: `src/main/resources/desktop.properties`

Co the truyen qua bien moi truong:

- `DB_HOST` (mac dinh `localhost`)
- `DB_PORT` (mac dinh `1433`)
- `DB_NAME` (mac dinh `QuanLyThuVien`)
- `DB_USERNAME` (mac dinh `sa`)
- `DB_PASSWORD` (mac dinh `123456`)

## Chay du an

### Cach 1: Maven

Neu may da cai Maven:

```bash
mvn clean compile exec:java
```

### Cach 2: IntelliJ / VS Code

- Chay class main: `com.library.desktop.Main`

## Cau truc

- `com.library.desktop.Main`: diem vao ung dung
- `com.library.desktop.ui.*`: cac man hinh Swing
- `com.library.desktop.dao.*`: truy van SQL truc tiep
- `com.library.desktop.db.*`: helper ket noi va query
- `com.library.desktop.model.*`: model record

## Luu y

- App desktop nay dung chung schema SQL Server voi ban web app.
- Khi xoa The loai/Sach/Doc gia, SQL Server co the bao loi FK neu du lieu dang duoc tham chieu.
