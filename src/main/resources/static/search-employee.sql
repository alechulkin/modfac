CREATE OR REPLACE PROCEDURE SearchEmployeeByName(
p_name IN VARCHAR2,
p_cursor OUT SYS_REFCURSOR
) AS
BEGIN
OPEN p_cursor FOR
SELECT * FROM EMPLOYEES
WHERE FIRST_NAME LIKE '%' || p_name || '%'
OR LAST_NAME LIKE '%' || p_name || '%';
END SearchEmployeeByName;