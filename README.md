btce-api
========

A small, fast Java library for the BTC-E bitcoin exchange.  The goal of this project is easy integration and easy use.

Small fast (and complete) Java API for the Cryptsy crypto-currency exchange with minimal dependencies.  The only external library needed to use this API is the [Google Gson Library](https://code.google.com/p/google-gson/).

Note: While the API is complete and functional in its current state, I am refining the naming conventions for the API and adding documentation.  This may require small but critical changes to your code if you upgrade to future versions.

## Installation

Add the file BTCE.java to your project and include the Gson jar as a reference.  Thats all thats needed.  The unit tests for this library use a properties file for saving the API keys but thats not a requirement.  The use of the properties file is only a requirement of the unit tests and not the API itself.

## Basic Usage

Create a BTCE API object using the following code:

```java
btce = new BTCE();
btce.setAuthKeys("<api_key>", "<api_secret>");
```

After this, using the API is as simple as calling the appropriate method off of your `cryptsy` object.  It is useful to look at the Cryptsy_Test.java source code since the unit tests in this file contain sample code for all of the APIs.  I'll be adding a complete javadoc in the near future.

## Examples

### Usage

Coming soon.

### Donate

Donations to any of the following addresses are appreciated:

BTC:  1CFP8cSbEkiKEjnadwdrtcKaARW3i8rsev

LTC:  LWD5zMujy3WoFHTdUpbNnzKCPAyD4EhgGF

BTC-E Code: abwaters

### Legal

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.