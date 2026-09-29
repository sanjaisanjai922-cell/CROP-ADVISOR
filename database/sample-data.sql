USE crop_advisor;

INSERT INTO region(name) SELECT 'Madurai' WHERE NOT EXISTS(SELECT 1 FROM region WHERE name='Madurai');
INSERT INTO region(name) SELECT 'Chennai' WHERE NOT EXISTS(SELECT 1 FROM region WHERE name='Chennai');
INSERT INTO region(name) SELECT 'Coimbatore' WHERE NOT EXISTS(SELECT 1 FROM region WHERE name='Coimbatore');

INSERT INTO officer(name,phone,email,region_id)
SELECT 'Arun Kumar','9000000001','arun@cropadvisor.local',id FROM region WHERE name='Coimbatore'
AND NOT EXISTS(SELECT 1 FROM officer WHERE email='arun@cropadvisor.local');

INSERT INTO officer(name,phone,email,region_id)
SELECT 'Priya Devi','9000000002','priya@cropadvisor.local',id FROM region WHERE name='Madurai'
AND NOT EXISTS(SELECT 1 FROM officer WHERE email='priya@cropadvisor.local');

INSERT INTO officer(name,phone,email,region_id)
SELECT 'Karthik Raj','9000000003','karthik@cropadvisor.local',id FROM region WHERE name='Chennai'
AND NOT EXISTS(SELECT 1 FROM officer WHERE email='karthik@cropadvisor.local');

INSERT INTO farmer(name,phone,email,crop,region_id)
SELECT 'Demo Farmer','9876543210','farmer@cropadvisor.local','Tomato',id FROM region WHERE name='Coimbatore'
AND NOT EXISTS(SELECT 1 FROM farmer WHERE email='farmer@cropadvisor.local');