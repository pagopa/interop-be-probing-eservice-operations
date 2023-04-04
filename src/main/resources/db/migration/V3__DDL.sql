CREATE VIEW eservice_view AS
SELECT e.id, e.eservice_id , e.eservice_name, e.producer_name , e.version_id , e.state ,e.probing_enabled , e.version_number , epr.response_received, epreq.last_request, e.polling_frequency
FROM eservices e
LEFT JOIN eservice_probing_responses epr ON epr.eservice_id = e.id
LEFT JOIN eservice_probing_requests epreq on epreq.eservice_id=e.id
GROUP BY e.eservice_name, e.producer_name , e.version_id , e.state, e.id, epr.response_received,epreq.last_request,e.polling_frequency;

