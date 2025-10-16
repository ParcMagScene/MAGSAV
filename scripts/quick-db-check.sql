-- Quick DB structure check
SELECT table_name, column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name IN ('users', 'utilisateurs', 'societes', 'companies', 'interventions')
ORDER BY table_name, ordinal_position;