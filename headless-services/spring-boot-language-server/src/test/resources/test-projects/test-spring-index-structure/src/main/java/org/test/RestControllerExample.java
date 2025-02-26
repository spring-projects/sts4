package org.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@RestController
public class RestControllerExample {
	
	@Autowired(required = false)
	private String serverPort;

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public String findOwner(@PathVariable(required = false) Integer ownerId) {
		return "example";
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@PostMapping("/owners/new")
	public String processCreationForm(String owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return "something";
		}

		return "redirect:/owners/";
	}
	
	@RequestMapping(path = "/owners/{ownerId}/edit", method = RequestMethod.POST)
	public String processUpdateOwnerForm(@Valid String owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
		return "redirect:/owners/{ownerId}";
	}

}
