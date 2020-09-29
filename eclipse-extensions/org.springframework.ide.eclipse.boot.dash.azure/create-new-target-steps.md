Creating  new 'Run Target Type' of boot dashboard.
---------------------------------------------------

These are the very roughly sketched steps taken to create the 'Azure Spring Cloud' target.

- Create a eclipse plugin to host your code
- make it depend on  `org.springframework.ide.eclipse.boot.dash`
- Setup a class to define your 'injections' via the boot dash's SimpleDIContainer
   - see org.springframework.ide.eclipse.boot.dash.azure.BootDashInjections
   - wire it into BootDash via eclipse extension point org.springframework.ide.eclipse.boot.dash.injections
- Create a class for your new RunTargetType (AzureRunTargetType).
   - make it extend AbstractRunTargetType
   - implement all abstract methods with simple stubs (e.g. many can throw 'not implemented yet' exceptsion)
   - Create a injection of this class in your 'BootDashInjections' class.

At this point you can run your creation in a Eclipse runtime workbench. When you access the pulldown menu
under the '+' icon in the dash your target should show (might not do anything yet when selected if you have
not implemented it yet.)

Implement a dialog UI that prompts the user for all the necessary infos to create a target
-------------------------------------------------------------------------------------------


  - authenticate with the platform
  - choose a target (i.e. in cf api url, user org, space), for Azure... credentials (oauth token), subscription, resource group, service


