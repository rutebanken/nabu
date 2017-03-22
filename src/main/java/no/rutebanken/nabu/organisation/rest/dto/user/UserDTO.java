package no.rutebanken.nabu.organisation.rest.dto.user;

import no.rutebanken.nabu.organisation.rest.dto.BaseDTO;

import java.util.List;


public class UserDTO extends BaseDTO {

	public String username;

	public String organisationRef;

	public List<String> responsibilitySetRefs;

	public ContactDetailsDTO contactDetails;

	public List<NotificationDTO> notifications;
}