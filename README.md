[![Contributors][contributors-shield]][contributors-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]

<br />
<p align="center">
  <a href="https://github.com/Lauriichan/JustLootIt">
    <!-- Add project image -->
    <!--<img src="" alt="Logo" width="512"/>-->
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

To get started with maven you first need to setup your environment to get access to the api maven package.
To do that simply go into your `.m2` folder which can be found at `%appData%\..\..\.m2` (Open `Run` on Windows and just paste the path into there and click `Ok`).
Afterwards if the file doesn't exist yet create the file `settings.xml` in the folder.
Then put following stuff into the file:
```XML
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

	<activeProfiles>
		<activeProfile>github</activeProfile>
	</activeProfiles>

	<profiles>
		<profile>
			<id>github</id>
			<repositories>
				<repository>
					<id>central</id>
					<url>https://repo1.maven.org/maven2</url>
				</repository>
				<repository>
					<id>github0</id>
					<url>https://maven.pkg.github.com/Lauriichan/*</url>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
				</repository>
			</repositories>
		</profile>
	</profiles>

	<servers>
		<server>
			<id>github0</id>
			<username>YOUR_GITHUB_USERNAME</username>
			<password>A_GITHUB_TOKEN</password>
		</server>
	</servers>
</settings>
```
Now replace `YOUR_GITHUB_USERNAME` with your github username.<br>
Then go to your Github account settings and scroll down until you see `Developer settings`.<br>
Go into the `Developer settings` and then click on `Personal access tokens`.<br>
Once you are there click on the `Generate new token` button.<br>
I would recommend you to enter `Maven packages` into the `Note` field and set the epiration to `No expiration`.<br>
Then you need to enable `repo:status` and `read:packages`. <br>
Once you enabled those click on `Generate Token` at the bottom.<br>
Now github should show you the token, simply copy the token and replace `A_GITHUB_TOKEN` with it.<br>
The token can be used for multiple servers so if you want to have access to another repository hosted on github simply copy the `github0` repository replace `MrNemo64` with the authors' name and copy the `github0` server. Be sure to rename the repository and server id to for example `github1` or something similar (they have to match up).<br>

Once this is setup you can simply add the dependency like this in your `pom.xml`:
```xml
<dependency>
  <groupId>me.lauriichan.spigot.justlootit</groupId>
  <artifactId>justlootit-core</artifactId>
  <version>1.0.0</version>
</dependency>
```
You can get the [latest version here](https://github.com/Lauriichan/JustLootIt/) (Comming soon)

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
Distributed under the MIT License. See `LICENSE` for more information.



<!-- CONTACT -->
## Contact

Project Link: [https://github.com/Lauriichan/JustLootIt](https://github.com/Lauriichan/JustLootIt)

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/Lauriichan/JustLootIt.svg?style=flat-square
[contributors-url]: https://github.com/Lauriichan/JustLootIt/graphs/contributors
[stars-shield]: https://img.shields.io/github/stars/Lauriichan/JustLootIt.svg?style=flat-square
[stars-url]: https://github.com/Lauriichan/JustLootIt/stargazers
[issues-shield]: https://img.shields.io/github/issues/Lauriichan/JustLootIt.svg?style=flat-square
[issues-url]: https://github.com/Lauriichan/JustLootIt/issues
