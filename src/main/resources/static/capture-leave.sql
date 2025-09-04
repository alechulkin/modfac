CREATE OR REPLACE PROCEDURE CaptureLeave(
p_employee_id IN NUMBER,
p_leave_type IN VARCHAR2,
p_start_date IN DATE,
p_end_date IN DATE,
p_status IN VARCHAR2,
p_approved_by IN NUMBER
) AS
v_balance NUMBER;
BEGIN
SELECT BALANCE INTO v_balance
FROM LEAVE_BALANCE
WHERE EMPLOYEE_ID = p_employee_id
AND LEAVE_TYPE = p_leave_type;
IF v_balance > 0 THEN
INSERT INTO LEAVES (EMPLOYEE_ID, LEAVE_TYPE, START_DATE, END_DATE,
STATUS, APPROVED_BY)
VALUES (p_employee_id, p_leave_type, p_start_date, p_end_date,
p_status, p_approved_by);
UPDATE LEAVE_BALANCE
SET BALANCE = BALANCE - 1
WHERE EMPLOYEE_ID = p_employee_id
AND LEAVE_TYPE = p_leave_type;
ELSE
RAISE_APPLICATION_ERROR(-20001, 'Insufficient leave balance');
END IF;

Unset
END CaptureLeave;