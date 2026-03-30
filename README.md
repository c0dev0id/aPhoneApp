# aPhoneApp - and Android Phone + Contact App for the DMD Remote 2

This Phone App implements the specification of the DMD Remote 2 and
is fully usable with the remote (the touch interface is completely
optional):
https://github.com/johnkeel-thork/DMD-For-Devs/blob/main/ControllerExample.java

We only react on Key Down with repeat=0 to avoid key repeat.

## Scope

- taking calls during motorcycle rides
- initiate calls from
  - a contact list
  - a calling history
  - a dialpad

## Out of scope

- creating contacts
- editing contacts

## Usage

### Incoming Call

1. Calling Card is shown as Overlay
   - Shows Name of Caller (from Contacts), Caller ID (if available), or "Unknown" (fallback)
   - Shows calling Phone Number
   - Has "Accept" Button (will activate with BUTTON 1 on the Remote)
   - Has "Decline" Button (will activate with BUTTON 2 on the Remote)
2. During the call:
   - The calling card stays visible
   - Shows call duration
   - Shows "End Call" Button (will activate with BUTTON 2 on the Remote)
3. On "Decline" or "End Call":
   - Close the calling card overlay
   - If no other overlay is open, quit the app

### Call Initiation

1. User starts "aPhoneApp" from the Home Screen
2. A sidecar overlay opens
3. two tabs "History" (default) and "Contacts" (can be changed with remote buttons LEFT and RIGHT)
   - History Tab: Shows calling history (incoming and outgoing calls), newest on top
     - Remote UP/DOWN navigates on the list
     - Remote BUTTON 1 initiates the call 
   - Contacts Tab: Shows all Contacts, Favorites on top, then alphabetical.
     - Remote UP/DOWN navigates on the list
     - Remote BUTTON 1 initiates the call
   - Dialpad Tab: A typical dialpad with display, numbers and call button
     - Remote Button 1 activates the dialpad (clearly visible focus change to the dialpad)
       - Remote LEFT/RIGHT/UP/DOWN will navigate the buttons on the dialpad
       - Remote BUTTON 1 will activate the focused button
       - Remote BUTTON 2 will return the focus back to the Tab control (and LEFT/RIGHT will change tabs again)
4. Remote BUTTON 2 closes the sidecar overlay and quits the app

Contacts List:
  - The contact list will show one entry per phone number. If Peter Muster has 2 phone numbers, he will appear twice in the list.
  - The list shows contacts as "Lastname, Firstname". The phone number is added in a smaller font size underneath as <type>: <number>.
    Example:
    Peter Muster              <-- bigger font
    mobile: +49(0)12345678    <-- smaller font

Call history:
  - The call history shows an icon that indicates if the entry represents an incoming (taken or missed) call or an outgoing (successful or not reached) call.
  - Then we show the phone number and contact information (if available).
    Example:
    ❌ +49(0)12345678 (Peter Muster)
  - Missed incoming calls will be shown in a red color

Incoming call while sidecar is open:
  - The sidecar is dimmed, and the calling card overlays the sidecar.
  - The incoming call takes priority and is therefore focused immediately.
  - The user must complete the incoming call process before focus is handed back to the sidecar.
  - When the call ends, the calling card closes, the sidecar undims, and focus returns to the sidecar.

Tab navigation direction:
  - The LEFT/RIGHT navigation does not wrap around on the last tab.
  

### Outgoing call

1. Calling Card is shown as Overlay
   - Shows Name of Contact that's being called.
   - Shows called Phone Number
   - Shows "Calling..." during dialing state
   - Shows call duration when connected
   - Shows "End Call" Button (will activate with BUTTON 2 on the Remote)
3. On "End Call":
   - Close the calling card overlay
   - If no other overlay is open, quit the app


## UI Design

The UI must be usable to be used during a motorcycle ride. Therefore we need big UI elements and font sizes.

- Text Sizes: 18, 22, 28
- Font: Apotek (copy from here: /home/sdk/androdash/app/src/main/res/font)

