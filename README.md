[![Contributors][contributors-shield]][contributors-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]

<br />
<p align="center">
  <a href="https://github.com/Lauriichan/JustLootIt">
    <img src="https://raw.githubusercontent.com/Lauriichan/JustLootIt/master/JustLootIt-Icon.png" alt="Logo" width="512"/>
  </a>

  <h3 align="center">JustLootIt</h3>
  <h4 align="center">Unique and refreshable loot containers and entities for everyone.</h4>

  <p align="center">
    <br />
    <a href="https://github.com/Lauriichan/JustLootIt/issues/new">Report Bug</a>
    ·
    <a href="https://github.com/Lauriichan/JustLootIt/issues/new">Request Feature</a>
  </p>
</p>




<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#container-and-entity-conversion">Container and entity conversion</a></li>
        <li><a href="#justlootit-as-maven-dependency">JustLootIt as Maven Dependency</a></li>
      </ul>
    </li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<!-- <img src="images/banner.png" alt="BetterInputs Banner"/> -->
JustLootIt is an alternative plugin to Lootin ([Spigot](https://www.spigotmc.org/resources/90453/) / [Github](https://github.com/sachingorkar102/Lootin-plugin/)).

The main reason for the existence of JustLootIt is that in 2023 Lootin was no longer maintained for a long time which then sparked this project as an alternative. However what I wanted to achieve is not just an alternative but more than that and I think I've done a pretty good job with it.
JustLootIt has the same functionality as Lootin and even more, it allows for refreshable containers (blocks and entities) and even linked containers (between multiple blocks and entities) and can be customized in many ways.
This allows players to loot a container more than one time if the server owner wants that to happen and even more is planned in the future.

### Built With

* [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse)
* [LayLib](https://github.com/Lauriichan/LayLib)
* [SpigotPluginBase](https://github.com/Lauriichan/SpigotPluginBase)
* [BetterInputs (optional)](https://github.com/MrNemo64/better-inputs)


<!-- GETTING STARTED -->
## Getting Started

To get a local copy you only need to download it from [Spigot](https://www.spigotmc.org/resources/justlootit.116493/)
or if you want to get the Source just fork this repository or download it as zip.

### Installation

To install the plugin you only need to do following steps:
1. Download the plugin
2. Put it into your server's plugin folder
3. Start or reload your server
4. Enjoy the plugin!

### Container and entity conversion

If you want to use JustLootIt in an already existing world that's no problem as well!<br/>
The plugin has a feature that allows you to convert vanilla loot containers and entities even if JustLootIt was installed at a later point in time, however it's not perfect and already looted containers might not be converted.<br>
If you came from Lootin then that's also no problem, it's the same process in this case, JustLootIt can just convert Lootin containers and entities to JustLootIt containers and entities.<br>
<b>Please note:</b> that in both cases the process is not revertable, so please create a backup of your world if you are not sure yet if you want to keep using JustLootIt in favour of vanilla or Lootin loot containers and entities.

Steps to convert vanilla or Lootin containers or entities:
1. Start your server
2. Type `/justlootit convert` into your console
3. Follow the setup process
4. Done!

The server will now restart (you may need to manually start the server if you did not setup a restart script in the `spigot.yml`) and do the conversion after which it will restart again if the process modified the worlds.

### JustLootIt as Maven Dependency

First you need to add the repository that JLI is published to:
```XML
<repositories>
  <repository>
    <id>lauriichan-release</id>
    <url>https://maven.lauriichan.me/release</url>
  </repository>
</repositories>
```

Afterwards you can simply add the dependency like this in your `pom.xml`:
```xml
<dependency>
  <groupId>me.lauriichan.spigot.justlootit</groupId>
  <artifactId>justlootit-core</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```
You can get the [latest version here](https://maven.lauriichan.me/#/release/me/lauriichan/spigot/justlootit/justlootit-core).

<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/Lauriichan/JustLootIt/issues) for a list of proposed features (and known issues).



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



<!-- LICENSE -->
## License
Distributed under the GPLv3 License. See `LICENSE` for more information.



<!-- CONTACT -->
## Contact

Discord Link: [https://discord.gg/m9vjsj6ScJ](https://discord.gg/m9vjsj6ScJ)

Project Link: [https://github.com/Lauriichan/JustLootIt](https://github.com/Lauriichan/JustLootIt)

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/Lauriichan/JustLootIt.svg?style=flat-square
[contributors-url]: https://github.com/Lauriichan/JustLootIt/graphs/contributors
[stars-shield]: https://img.shields.io/github/stars/Lauriichan/JustLootIt.svg?style=flat-square
[stars-url]: https://github.com/Lauriichan/JustLootIt/stargazers
[issues-shield]: https://img.shields.io/github/issues/Lauriichan/JustLootIt.svg?style=flat-square
[issues-url]: https://github.com/Lauriichan/JustLootIt/issues
