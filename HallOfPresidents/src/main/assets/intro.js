{
    "version": 1,
    "background": "background.png",
    "furniture": "furniture.png",
    "terrain": "terrain.png",
    "victory": {
        "condition_room_flags": [ "HEARD_ORANGE", "HEARD_GREEN", "HEARD_BLUE", "HEARD_YELLOW" ],
        "dialog": {
            "facing": "Down",
            "dialog": "CONGRATULATIONS!\n\nYou've learned the wisdom of the four elemental goddesses!\nYou're well on your way to being an epic hero! Probably!"
        }
    },
    "events": [
        {
            "bounds": {
                "left": 264,
                "top": 980,
                "right": 453,
                "bottom": 1157
            },
            "name": "WEST EVENT",
            "dialog": {
                "facing": "Up",
                "set_room_flags": [ "HEARD_ORANGE" ],
                "dialog": "ORANGE GODDESS:\n\nI haven't read Joseph Campbell, but I have seen \"Star Wars\""
            }
        },
        {
            "bounds": {
                "left": 742,
                "top": 629,
                "right": 928,
                "bottom": 808
            },
            "name": "NORTHWEST EVENT",
            "dialog": {
                "facing": "Up",
                "set_room_flags": [ "HEARD_GREEN" ],
                "dialog": "GREEN GODDESS:\n\nI haven't seen the original Star Wars, but I have seen \"The Phantom Menace\""
            }
        },
        {
           "bounds": {
                "left": 1280,
                "top": 637,
                "right": 1469,
                "bottom": 816
           },
            "name": "NORTHEAST EVENT",
            "dialog": {
                "facing": "Up",
                "set_room_flags": [ "HEARD_BLUE" ],
                "dialog": "BLUE GODDESS:\n\nI haven't seen \"The Phantom Menace\", but I did play Final Fantasy VII."
            }
        },
        {
            "bounds": {
                "left": 1742,
                "top": 981,
                "right": 1928,
                "bottom": 1160
            },
            "name": "EAST EVENT",
            "dialog": {
                "facing": "Up",
                "set_room_flags": [ "HEARD_YELLOW" ],
                "dialog": "YELLOW GODDESS:\n\nI haven't played any Final Fantasy, but I do read the fan fiction."
            }
        }
    ]
}
