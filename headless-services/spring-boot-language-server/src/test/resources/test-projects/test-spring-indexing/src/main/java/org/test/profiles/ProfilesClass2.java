package org.test.profiles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("profile2")
public class ProfilesClass2 {
}
