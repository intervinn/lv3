# lv3
This is a tiny server-side fabric mod that
gives all players only 3 lives initially.

You may do whatever with your lives, waste them or give to someone else.

A player without lives turns into a ghost and can only play in spectator mode.
If someone exchanges lives with them, they would get revived.

Mod doesn't require any configuration and provides only two commands:
* `/exchange [player]` - give one your life to someone else, if you had one life you would turn into a ghost, if the receiver was ghost then they get revived
* `/lives` - displays your amount of lives
* `/addlive` - give one life to a player, requires you to be an operator