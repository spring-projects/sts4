package org.test.profiles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = {"profile1", "profile2"})
public class ProfilesClassWithArray {

}
