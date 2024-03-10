# scccpdebris
Optional public part, The aim is to fix flawed part of the Minecraft multiplayer protocol
# feature:
All features were added without affecting vanilla. Requires **ProtocolLib**!
- [X] Randomize entity health to prevent players from using cheats to see it.
- [X] Hide player's detailed potion effects to avoid other cheating players from seeing
- [X] Randomized achievement details to prevent players from using [cheat mod](https://modrinth.com/mod/advancementinfo) to view achievements.
- [X] Reduce the number of chunks sent during the Nether and Dragon battles to alleviate the no fog cheating. At the same time reduce the network burden.
- [X] Detect [Some cheats remove blindness effect](https://www.curseforge.com/minecraft/mc-mods/cat-eyes-night-vision-toggle-moda), and Reduces the number of chunks sent when affected by blindness and darkness to reduce cheating hazards
- [ ] Detect freecam by whether the player does not send move packets (not necessarily stable, it is disabled by default in the configuration file)
    
For hide-durability and hide-itemmeta, you can turn it on in [paper settings](https://docs.papermc.io/paper/reference/world-configuration#obfuscation)
