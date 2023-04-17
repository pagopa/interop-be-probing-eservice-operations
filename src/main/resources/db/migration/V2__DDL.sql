CREATE TABLE ${schema_name}.eservice_probing_responses (
	response_received timestamptz NOT NULL,
	eservices_record_id int8 NOT NULL,
	CONSTRAINT eservice_probing_responses_pkey PRIMARY KEY (eservices_record_id)
);

ALTER TABLE ${schema_name}.eservice_probing_responses ADD CONSTRAINT FK_ESERVICE_ESERVICE_PROBING_RESPONSE FOREIGN KEY (eservices_record_id) REFERENCES ${schema_name}.eservices(id);

CREATE TABLE ${schema_name}.eservice_probing_requests (
	last_request timestamptz NOT NULL,
	eservices_record_id int8 NOT NULL,
	CONSTRAINT eservice_probing_requests_pkey PRIMARY KEY (eservices_record_id)
);

ALTER TABLE ${schema_name}.eservice_probing_requests ADD CONSTRAINT FK_ESERVICE_ESERVICE_PROBING_REQUESTS FOREIGN KEY (eservices_record_id) REFERENCES ${schema_name}.eservices(id);

delete from ${schema_name}.eservice_probing_requests;
delete from ${schema_name}.eservice_probing_responses ;
delete from ${schema_name}.eservices;

ALTER TABLE ${schema_name}.eservices ADD lock_version int NOT NULL;
ALTER TABLE ${schema_name}.eservices ADD version_number int NOT NULL;






