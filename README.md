# EasyGym  

EasyGym is an unofficial sidemod for [Pixelmon Reforged](https://reforged.gg). It adds a gym system where players can battle gym leaders to earn badges. The advantage of this plugin over others is that EasyGym allows gym leaders to import teams straight from pastebin in Showdown format. Use the paste code [JrJ4Um38](https://pastebin.com/JrJ4Um38) when importing to see an example team.

# WARNING

This plugin has not been tested fully. Improper team imports may crash the server when loaded. It is highly reccomended to test all team imports on a local test server before used in production. Please report any bugs that you find to this github so that they may be fixed.

## Download

[1.1](https://www.dropbox.com/s/4jy909hizw2typ8/easygym-1.1.jar?dl=0)

# Usage

## GymTeam

Text for `/gymteam help`

Usage: `/gymteam mode [args]`

`/gymteam import gym pastecode teamname`  
Imports a team from the pastecode into the gym  

`/gymteam delete gym teamname`  
Deletes a team from the gym   

`/gymteam list [gym]`  
Provides GUI for choosing teams. Also lists either all gyms or all teams in a gym

`/gymteam load gym teamname`  
Loads a team from a gym  

`/gymteam clear`  
DANGER: CLEARS ALL POKEMON FROM PARTY  

`/gymteam upload`  
Uploads the user's party team to the gym


## Gyms

Text for `/gyms help`  

Usage: `/gyms [mode] [args]`  

`/gyms`  
Shows gym GUI  

`/gyms add type badge color [lvlcap] [priority]`  
Adds a gym of given type, badge, and badge color. User can optionally supply a level cap or priority. A lower priority means the gym will appear first  

`/gyms leader add gym gymleader`  
Adds gymleader as a leader to the gym  

`/gyms leader remove gym gymleader`  
Adds gymleader as a leader to the gym  

`/gyms reward add gym`  
Adds items in hand as reward for beating gym  

`/gyms reward clear gym`  
Clears all rewards from gym  

`/gyms givebadge gym player`  
Gives badge and rewards to player.  

# Permissions:

## Commands
`easygym.command.gymteam`  
Allows player to use the /gymteam command. Player still need proper action permissions to make subcommands.

`easygym.command.gyms`  
Allows player to use the /gyms command. Player still need proper action permissions to make subcommands.

## GymTeam Actions

`easygym.actions.gymteam.import`  
Allows player to import a gymteam from pastebin

`easygym.actions.gymteam.deleted`  
Allows player to delete a gymteam

`easygym.actions.gymteam.list`  
Allows player to see the available gymteams available

`easygym.actions.gymteam.load`  
Allows player to load a gymteam

`easygym.actions.gymteam.clear`  
Allows player to delete all pokemon in party

## GymTeam Actions

`easygym.actions.gyms.add`  
Allows player to add a gym

`easygym.actions.gyms.delete`  
Allows player to delete a gym

`easygym.actions.gyms.leader`  
Allows player to add or remove gym leaders

`easygym.actions.gyms.reward`  
Allows player to add or clear additional rewards for beating the gym

`easygym.actions.gyms.givebadge`  
Allows player to give another user a gym's badge and any additional rewards. Player still needs to be a gym leader of the gym to give its badge.