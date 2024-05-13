CREATE DATABASE hinderappdb;
USE hinderappdb;

CREATE TABLE Member(Name VARCHAR(50) UNIQUE, Email VARCHAR(40) UNIQUE, password VARCHAR(20));
CREATE TABLE Victim(Description VARCHAR(250), Age INT, SSN VARCHAR(11) UNIQUE, Address VARCHAR(50), firstName VARCHAR(20) UNIQUE, lastName VARCHAR(20) UNIQUE, victimID INT UNIQUE);
CREATE TABLE PrivateMessage(Date DATE, Content VARCHAR(250), MsgID INT UNIQUE, Email VARCHAR(40), Recipient VARCHAR(40),FOREIGN KEY (Email) REFERENCES Member(Email), FOREIGN KEY (Recipient) REFERENCES Member(Name));
CREATE TABLE Comment(Date DATE, Content VARCHAR(250), Name2 VARCHAR(50), commentID INT UNIQUE, SSN VARCHAR(11), FOREIGN KEY (SSN) REFERENCES Victim(SSN), FOREIGN KEY(Name2) REFERENCES Member(Name)); 
CREATE TABLE recentFive(firstName VARCHAR(50), lastName VARCHAR(50), victimID int,foreign key(firstName) REFERENCES Victim(firstName), foreign key(lastName) references Victim(lastName), foreign key(victimID) references Victim(victimID));


/*Shift all victimIDs down 1 if anything before it is deleted*/
DELIMITER //

CREATE TRIGGER DeletingVictim
BEFORE DELETE ON Victim
FOR EACH ROW
BEGIN 
	DELETE FROM Comment
    WHERE Comment.SSN = Old.SSN;
    
END //

/* The trigger for the 5 most recent hinder recipients. */
CREATE TRIGGER OnVictimAdd
AFTER INSERT ON VICTIM
FOR EACH ROW
BEGIN
	IF(SELECT COUNT(*) FROM recentFive) >= 5 THEN
		DELETE FROM recentFive ORDER BY victimID ASC LIMIT 1;
	END IF;
    
    INSERT INTO recentFive(firstName, lastName, victimID)
    VALUES (NEW.firstName, NEW.lastName, NEW.victimID);
END //

/* When we delete a member we also delete all private messages that existed. */
CREATE TRIGGER DeleteMemberEvent
BEFORE DELETE ON Member
FOR EACH ROW
BEGIN 
	DELETE FROM PrivateMessage
    WHERE PrivateMessage.Email = Old.Email OR PrivateMessage.Recipient = Old.Email;
END //

/* Gets all victims in the victim table */
CREATE PROCEDURE getAllVictims()
BEGIN
	SELECT firstName, lastName, SSN
    FROM Victim;
END //

DELIMITER ;

/* Shows all the members on the website currently */
CREATE VIEW usernames AS 
	SELECT Name 
	FROM Member;
    
CREATE VIEW commentSSN AS
	SELECT firstName, lastName, SSN
    FROM Victim;

CREATE VIEW commentGetIds AS 
	SELECT Content, commentID, Name2
    FROM Comment;


/* Population HERE */
INSERT INTO Member VALUES('KrisMiddleTon48', 'miggy2424@gmail.com', '12345apples'), 
('MikeTysonFan9872', 'iluvmike23@hotmail.com', 'mikeoverlebron'), 
('ChickenSalsaFingers', 'fingerlickin@yahoo.com', 'chickensalsa');

INSERT INTO Victim VALUES('Built like a chicken skewer.', 24, '789-11-8989', '33 Wallaby Way', 'Rob', 'Miller', 1),
('Has short hair and a long neck.', 35, '190-22-2785', '42 Shelby Ave', 'Steve', 'Patron', 2),
('Is ugly has an addiction to pixie stix.', 19, '865-42-9090', '1873 Christian St', 'Godeer', 'Valyn', 3),
('Chicken parm eater, will not move much.', 22, '123-52-0125', '9022 Multi Ct', 'John', 'Buckingham', 4),
('Does not like broccoli, will spit in your direction.', 29, '897-11-0924', '1234 Shoreline St', 'Isa', 'Llama', 5),
('testing1234.', 25, '913-23-1090', '23 Beachhead Ave', 'Lebron', 'James', 6); 
/*Population END*/