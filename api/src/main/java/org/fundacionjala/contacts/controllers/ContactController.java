package org.fundacionjala.contacts.controllers;

import org.fundacionjala.contacts.db.entities.ContactData;
import org.fundacionjala.contacts.exceptions.ContactNotFoundException;
import org.fundacionjala.contacts.exceptions.DuplicatedContactException;
import org.fundacionjala.contacts.exceptions.RequiredFieldException;
import org.fundacionjala.contacts.models.Contact;
import org.fundacionjala.contacts.repository.ContactRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping("/contacts")
    public List<Contact> retrieveAllContacts(@RequestParam(required = false) String name) {
        if (name == null || name.isEmpty()) {
            return contactRepository
                    .findAll()
                    .stream()
                    .map(ContactData::toModel)
                    .collect(Collectors.toList());
        }

        return contactRepository
                .findByName(name)
                .stream()
                .map(ContactData::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/contacts/{id}")
    public Contact retrieveContactById(@PathVariable Long id) throws ContactNotFoundException {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Unable to find contact with Id: " + id))
                .toModel();
    }

    @PostMapping("/contacts")
    public Contact saveContact(@RequestBody Contact contact) throws RequiredFieldException {
        validateContactFields(contact);
        return contactRepository
                .save(contact.toEntity())
                .toModel();
    }

    @DeleteMapping("/contacts/{id}")
    public String deleteContactById(@PathVariable Long id) throws ContactNotFoundException {
        ContactData contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException("Not found error code - Unable to find contact with Id: " + id));

        contactRepository.delete(contact);
        return "DELETED";
    }

    private void validateContactFields(Contact contact) throws RequiredFieldException {
        if (contact.getEmail() == null || contact.getEmail().isEmpty()) {
            throw new RequiredFieldException("email");
        }

        if (contact.getName() == null || contact.getName().isEmpty()) {
            throw new RequiredFieldException("name");
        }

        Optional<ContactData> existingContact = contactRepository.findByEmail(contact.getEmail());

        if (existingContact.isPresent()) {
            throw new DuplicatedContactException("Unable to create contact due duplicated email: " + contact.getEmail());
        }
    }
}
