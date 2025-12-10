# Characters summonned
summon Damon, 500
summon Elena, 300

# Increase HP
feed Damon, Elena
drain Elena, Damon

# Protection bit turned on (1), so character is protected
daylight_ring Elena
daytime Elena, 20

# if equal, branches to equal_label (fight to reduce HP)
fangs Damon, Elena, equal_label
# jump/"disappear" to skip label
disappear skip


equal_label:
feed Elena, Damon

# character is staked, kill bit becomes on (1)
skip:
stake Damon, Elena

end:
