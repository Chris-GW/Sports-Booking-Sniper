[![Sports-Booking-Sniper](img/Sports%20Booking%20Sniper%20Logo.svg)](https://github.com/Chris-GW/Sports-Booking-Sniper)


# Sports Booking Sniper

[![Latest-Release](https://img.shields.io/github/v/release/Chris-GW/Sports-Booking-Sniper?include_prereleases)](https://github.com/Chris-GW/Sports-Booking-Sniper/releases)
[![Java CI with Maven](https://github.com/Chris-GW/Sports-Booking-Sniper/actions/workflows/maven.yml/badge.svg)](https://github.com/Chris-GW/Sports-Booking-Sniper/actions/workflows/maven.yml)
[![GitHub issues](https://img.shields.io/github/issues/Chris-GW/Sports-Booking-Sniper)](https://github.com/Chris-GW/Sports-Booking-Sniper/issues)
[![GitHub stars](https://img.shields.io/github/stars/Chris-GW/Sports-Booking-Sniper)](https://github.com/Chris-GW/Sports-Booking-Sniper/stargazers)
[![GitHub license](https://img.shields.io/github/license/Chris-GW/Sports-Booking-Sniper)](https://github.com/Chris-GW/Sports-Booking-Sniper/blob/main/LICENSE)

> Console GUI with selenium web booking agent for [HSZ RWTH Aachen](https://hochschulsport.rwth-aachen.de/cms/~icgi/HSZ/) sports offer

[![Sports-Booking-Sniper](img/Main%20GUI%20Screenshot%20.png)](https://github.com/Chris-GW/Sports-Booking-Sniper)
![Sport Buchung Personenangaben Formular.gif](img/Sport%20Buchung%20Personenangaben%20Formular.gif)
---

## Installation
Sports Booking Sniper requires Maven to be running on JRE 11+.  
Releases are available on the [Sports Booking Sniper release page](https://github.com/Chris-GW/Sports-Booking-Sniper/releases).

```shell
mvn package
java -jar sportbooking-{version}-jar-with-dependencies.jar
```

---

## Features

- Simple console GUI with [Lanterna](https://github.com/mabe02/lanterna)  
Lanterna is a Java library allowing you to write easy semi-graphical user interfaces in a text-only environment, very similar to the C library curses but with more functionality.
- Selenium Agent automatically performs the sports bookings for you in the background.
- Supports the booking of many different [HSZ RWTH Aachen](https://hochschulsport.rwth-aachen.de/cms/~icgi/HSZ/) sports offers. 
  - Sports tickets for a whole semester
  - single date sport bookings 
  - single sport place bookings
- Works great on raspberry pi or any other single board computers

---

## Contributors

| Developer | 
| :---: |
| [![Chris-GW](https://avatars0.githubusercontent.com/u/8419701?s=200&u=0c42657351c46ae8be0e01fa2fe313d091c2bebc&v=4)](https://github.com/Chris-GW) |    
| <a href="https://github.com/Chris-GW" target="_blank">`github.com/Chris-GW`</a> | 

| Muse | 
| :---: |
| [![lukszi](https://avatars0.githubusercontent.com/u/25265369?s=200&u=49c6d43f12fb04de40252e626b32e983500c04e7&v=4)](https://github.com/lukszi) |    
| <a href="https://github.com/lukszi" target="_blank">`github.com/lukszi`</a> | 


---

## FAQ

- **How do I do *specifically* so and so?**
    - No problem! Just do this.

---

## License

[![GitHub license](https://img.shields.io/github/license/Chris-GW/Sports-Booking-Sniper)](https://github.com/Chris-GW/Sports-Booking-Sniper/blob/main/LICENSE.md)

- **[AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html)**
