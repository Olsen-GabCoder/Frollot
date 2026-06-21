-- Fix FK bookings.service_id: pointe vers 'services' (vide/obsolete)
-- alors que les services sont dans 'salon_services'.
ALTER TABLE bookings DROP FOREIGN KEY bookings_ibfk_4;
ALTER TABLE bookings ADD CONSTRAINT fk_booking_service
    FOREIGN KEY (service_id) REFERENCES salon_services(id) ON DELETE RESTRICT;
