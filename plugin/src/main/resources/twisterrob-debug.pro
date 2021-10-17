### -- twister-plugin-gradle/twisterrob-debug.pro -- ###
# Conditionally added when releasing (depending on buildType.debuggable)

# debugger support
-keepattributes LocalVariableTable,LocalVariableTypeTable
-optimizations !code/allocation/variable

# Do some optimizations, but keep the build time to a minimum.
-optimizationpasses 1

# Debug helper options
#-dontobfuscate # to keep long class/method names
