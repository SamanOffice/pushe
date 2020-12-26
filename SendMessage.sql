USE [Sepahvand]
GO

/****** Object:  StoredProcedure [chat].[SendMessage]    Script Date: 12/26/2020 10:19:43 AM ******/
DROP PROCEDURE [chat].[SendMessage]
GO

/****** Object:  StoredProcedure [chat].[SendMessage]    Script Date: 12/26/2020 10:19:43 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


create  PROC [chat].[SendMessage] (@SenderUserId INT,@ReceiverUserId INT NULL,@GroupId INT NULL,@MessageType INT,@Message NVARCHAR(MAX),@Data NVARCHAR(MAX),@FileName NVARCHAR(MAX))
AS 
BEGIN

IF(@GroupId IS NOT NULL) 
BEGIN
	DECLARE @HasAccess INT = (SELECT top 1 p.Id FROM chat.Participants p 
	LEFT JOIN UserRoles ur ON ur.UserId = @SenderUserId AND ur.RoleId = p.RoleId
	WHERE GroupId = @GroupId AND (p.RoleId IS NULL OR ur.UserId = @SenderUserId)
							 AND (p.UserId IS NULL OR p.UserId = @SenderUserId)
							 AND NOT (p.UserId IS NULL AND p.RoleId IS NULL))

	IF (@HasAccess IS NULL)
	BEGIN
	 RAISERROR('User has not access to send message in group',1,1);
	 RETURN;
	END

	GOTO InsertMessage;
END


SET @GroupId = (SELECT TOP 1 g.Id FROM chat.Participants p1 
LEFT JOIN chat.Participants P2 ON P1.GroupId = P2.GroupId AND P1.Id <> P2.Id
LEFT JOIN chat.Groups g ON g.Id = p1.GroupId
WHERE g.[Type] = 0 AND ((p1.UserId = @SenderUserId AND p2.UserId = @ReceiverUserId ) OR (p2.UserId = @SenderUserId AND p1.UserId = @ReceiverUserId )))

IF(@GroupId IS NULL)
BEGIN

INSERT INTO chat.Groups VALUES (NULL,0,NULL)
SET @GroupId = (SELECT scope_identity())

INSERT INTO chat.Participants (GroupId,UserId,Writable,Admin) values (@GroupId,@SenderUserId,1,1)
INSERT INTO chat.Participants (GroupId,UserId,Writable,Admin) values (@GroupId,@ReceiverUserId,1,1)

END

InsertMessage:
INSERT INTO chat.[Messages] VALUES (@GroupId,@SenderUserId,@MessageType,0,@Message,@Data,GETDATE(),@FileName)


END
GO

