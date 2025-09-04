CREATE OR REPLACE PROCEDURE OnboardEmployee(
p_first_name IN VARCHAR2,
p_last_name IN VARCHAR2,
p_email IN VARCHAR2,
p_phone_number IN VARCHAR2,
p_hire_date IN DATE,
p_job_id IN VARCHAR2,
p_salary IN NUMBER,
p_manager_id IN NUMBER,
p_street IN VARCHAR2,
p_city IN VARCHAR2,
p_state IN VARCHAR2,
p_zip_code IN VARCHAR2,
p_created_by IN NUMBER
) AS
v_employee_id NUMBER;
BEGIN
-- Check if the user is an admin
DECLARE
v_role VARCHAR2(20);

BEGIN
SELECT ROLE INTO v_role FROM USERS WHERE USER_ID = p_created_by;
IF v_role != 'ADMIN' THEN
RAISE_APPLICATION_ERROR(-20002, 'Only admin users can create
employees');
END IF;
END;
SELECT EMPLOYEE_ID INTO v_employee_id
FROM EMPLOYEES
WHERE FIRST_NAME = p_first_name
AND LAST_NAME = p_last_name
AND PHONE_NUMBER = p_phone_number;
IF v_employee_id IS NOT NULL THEN
UPDATE EMPLOYEES
SET EMAIL = p_email,
HIRE_DATE = p_hire_date,
JOB_ID = p_job_id,
SALARY = p_salary,
MANAGER_ID = p_manager_id
WHERE EMPLOYEE_ID = v_employee_id;
UPDATE ADDRESSES
SET STREET = p_street,
CITY = p_city,
STATE = p_state,
ZIP_CODE = p_zip_code
WHERE EMPLOYEE_ID = v_employee_id;
ELSE
INSERT INTO EMPLOYEES (FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER,
HIRE_DATE, JOB_ID, SALARY, MANAGER_ID)
VALUES (p_first_name, p_last_name, p_email, p_phone_number,
p_hire_date, p_job_id, p_salary, p_manager_id);
SELECT EMPLOYEE_ID INTO v_employee_id
FROM EMPLOYEES
WHERE FIRST_NAME = p_first_name
AND LAST_NAME = p_last_name
AND PHONE_NUMBER = p_phone_number;
INSERT INTO ADDRESSES (EMPLOYEE_ID, STREET, CITY, STATE, ZIP_CODE)
VALUES (v_employee_id, p_street, p_city, p_state, p_zip_code);
END IF;
EXCEPTION
WHEN NO_DATA_FOUND THEN
INSERT INTO EMPLOYEES (FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER,
HIRE_DATE, JOB_ID, SALARY, MANAGER_ID)

Unset
VALUES (p_first_name, p_last_name, p_email, p_phone_number,
p_hire_date, p_job_id, p_salary, p_manager_id);
SELECT EMPLOYEE_ID INTO v_employee_id
FROM EMPLOYEES
WHERE FIRST_NAME = p_first_name
AND LAST_NAME = p_last_name
AND PHONE_NUMBER = p_phone_number;
INSERT INTO ADDRESSES (EMPLOYEE_ID, STREET, CITY, STATE, ZIP_CODE)
VALUES (v_employee_id, p_street, p_city, p_state, p_zip_code);
END OnboardEmployee;