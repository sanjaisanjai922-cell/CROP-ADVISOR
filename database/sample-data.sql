USE crop_advisor;
INSERT INTO region(name) SELECT 'Madurai' WHERE NOT EXISTS(SELECT 1 FROM region WHERE name='Madurai');
INSERT INTO region(name) SELECT 'Chennai' WHERE NOT EXISTS(SELECT 1 FROM region WHERE name='Chennai');
INSERT INTO region(name) SELECT 'Coimbatore' WHERE NOT EXISTS(SELECT 1 FROM region WHERE name='Coimbatore');