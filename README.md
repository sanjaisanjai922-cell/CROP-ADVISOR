# Crop Advisor

Crop Disease Query and Advisory Ticketing System built with Java 17, Spring Boot, Maven, HTML, CSS, JavaScript, MySQL and Apache/XAMPP.

## IntelliJ IDEA
1. Start MySQL in XAMPP.
2. Create the `crop_advisor` database in phpMyAdmin.
3. Open this Maven project in IntelliJ IDEA.
4. Run `CropAdvisorApplication`.
5. Copy the `frontend` folder to `C:\\xampp\\htdocs\\crop-advisor`.
6. Open `http://localhost/crop-advisor/`.

Spring Boot uses JPA/Hibernate to create the application tables automatically.

## API
- `GET /api/regions`
- `GET /api/farmers`
- `GET /api/officers`
- `GET /api/tickets`
- `POST /api/tickets`
- `GET /api/farmers/{id}/tickets`
- `PUT /api/tickets/{id}/status?value=CLOSED`
