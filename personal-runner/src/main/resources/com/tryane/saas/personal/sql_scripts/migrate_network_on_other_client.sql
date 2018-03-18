DO
$$
DECLARE 
idOldClient bigint := 1;
idNewClient := 2;
BEGIN
	update core_client set clientid = idNewClient where clientid = idOldClient;
	update core_clientprop set clientid = idNewClient where clientid = idOldClient;
	update core_network set clientid = idNewClient where clientid = idOldClient;
	update core_user set clientid = idNewClient where clientid = idOldClient;
END 
$$;