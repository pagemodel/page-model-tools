This page describes utility classes useful for testing with Page Model Tools.

# Unique
Generate unique data from random UUIDs
### Unique.string(String format)
Returns a unique string from the provided format.

Each occurrence of `%s` in the format string is replaced with a random 12 character alpha-numeric string.

### Unique.shortString()
Returns a random 12 character alpha-numeric string derived from a uuid.

### Unique.longString()
Returns a random UUID as  a string.

### Unique.uuid()
Returns a random UUID.

### Unique.long()
Returns a long derived from a random UUID.

# ProfileMap
Load a map of profile names to record objects from json (using Gson).

# SystemProperties
Read properties for gradle arguments, environment variables, and the `user.defaults` file.