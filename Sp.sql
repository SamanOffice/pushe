USE [Sepahvand]
GO
/****** Object:  StoredProcedure [chat].[SpSendMessage]    Script Date: 12/26/2020 4:04:32 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

--Create Procedure 
ALTER   PROCEDURE  [chat].[SpSendMessage]   (@GroupId int ,@SenderUserId int ,@ReceiverUserId INT,@MessageType INT,@Message NVARCHAR(MAX),@Data NVARCHAR(MAX),@FileName NVARCHAR(MAX))
As 
BEGIN

DECLARE  @IsGroupExist  int =(select  Id from chat.Groups where Id=@GroupId);
DECLARE  @isAdmin  int=(select top 1(UserId) from chat.Participants where UserId=@SenderUserId and admin=1 and GroupId=@GroupId);



---Intrduction 
 if(@ReceiverUserId =0)
			if(@IsGroupExist is not  null)
		    	begin
				  if(@isAdmin is not null)
						begin
							select DISTINCT  gg.Name ,p.GroupId, t.UserId, t.token 
								from chat.Participants p 
									left join UserRoles r
									on r.RoleId=p.RoleId 
									left  join Tablet.TabletUsers t
										on t.UserId=r.UserId
										or t.UserId=p.UserId 
										or t.WareHouseId = p.WareHouseId 
									left join chat.Groups gg
										on  gg.Id=p.GroupId

							where p.GroupId=@GroupId and  t.Token is  not null  and t.UserId <>@SenderUserId
							GOTO  InsertMessage
						end

				  ELSE
						BEGIN
      
					RAISERROR('user  cant send message in group',16,1);
					return
					END
			    END
			ELSE
				BEGIN
					RAISERROR('group doese not exist',16,1);
					 goto InsertGroup
				END  
 ELSE 
	begin
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
										return 

							    END
							else
								begin
												select DISTINCT  gg.Name ,p.GroupId, p.UserId, t.token 
													from chat.Participants p 
													left join Tablet.TabletUsers t
													on t.UserId=p.UserId 
													left join chat.Groups gg
												on  gg.Id=p.GroupId
												where p.GroupId=@GroupId and  t.Token is  not null and t.UserId <>@SenderUserId
												GOTO InsertMessage
								end


	
    END 

InsertMessage:
	BEGIN
	INSERT INTO chat.Messages  values (@GroupId,@SenderUserId,@MessageType,0,@Message,@Data,GETDATE(),@FileName)
	 return
	END
	
InsertGroup:
	begin
	INSERT INTO chat.Groups  values (null,@MessageType,null)
	END



END 

