# Storage Adapters

| Id   | Name                   | Availability    |
|------|------------------------|-----------------|
| 0    | CachedInventory        | Player          |
| 1    | CacheLookupTable       | Player          |
| 14   | FrameContainer         | Level Container |
| 15   | StaticContainer        | Level Container |
| 16   | VanillaContainer       | Level Container |
| 17   | CompatibilityContainer | Level Container |

# Player Storage

| Id      | Name             |
|---------|------------------|
| 0 - 14  |                  |
| 15      | CacheLookupTable |
| 16 - 63 | CachedInventory  |

# Level Container Storage

Dynamic storage for containers that are saved for a level. This storage has no defined ids for certain objects, any object that is possible to be saved in this storage can have any id that the storage method provides.
