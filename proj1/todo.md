# Current biggest problem for RECLAIM:
PUTCHUNK protocols are only cancelled if the PUTCHUNK is received during the "grace period" (0-400ms delay)
The delay also seems to not be working in terms of stopping the PUTCHUNKs?

## Important TODOs (Essential):

- ~~Store information for the files that the peer requested to be backed up (initiator peer)~~
    - ~~(peer that does backups should store information just like ChunkBackupState -> necessary for STATE)~~
- ~~Exit handler and recover on startup for the backup information~~
- consider protocol version and other things like that in message parsing:
    - ~~Reading body from correct CRLFs~~ - done and working
    - Not yet looking at the protocol version
- ~~discuss the reclaim strategy:~~ - Done
  - Current idea:
      - Try removing chunks that already have over the minimum required replication degree
      - After that, start by the ones that have the smallest size (because it might be easier to fit them elsewhere)
- ~~todo: handling REMOVED with PUTCHUNKs to the network if the replication degree has fallen, etc~~ - done, needs testing
- ~~Fix RECLAIM repeated PUTCHUNKS~~ - Was fine after all, but see below
- Test RECLAIM more extensively, especially the stopping of crossed PUTCHUNKs (maybe the "bug" was simply one doing the backup of one chunk and the other of the other one)

### Enhancements:

- Restore enhancement, use passive FTP idea with ServerSocket with a simple timeout (setSoTimeout)
- Backup enhancement? Need to bounce off some ideas

## Relvant TODOs:

- Discuss file\_id generation implementation:
    - Currently only the file name and sender id are being considered due to the desire to be able to re-encode without needing the original file
    - However, this does not consider different absolute paths (probably should hash using it instead of simply the file name)
    discuss discuss discuss
- Don't load the whole file at once, split it directly when reading -> must change structure to start sending tasks as the file chunks are read for it to make any difference :/
- If any chunk fails in backup, cancel everything and send DELETE

## Minor-ish TODOs:
- adicionar ao chunkbackupstate mal se recebe o putchunk, apagar se der erro (assim apanham-se todos os STOREDs ou mais vá) -> does not seem like a priority
- ~~delete must also look at the peer's own files lol <- Very easy but important~~ - done but cannot check due to the sender id field encoding the file id
- maybe dont make so many copies of byte arrays when it is not necessary (such as copying the body of a message with CommonMessage.getBody())

### To sort:

enhancements if there is time:
- Sliding window for Restore protocol (instead of Stop & Wait like currently implemented) -> HashMap idea with collecting everthing in the end is fine probably
- If a file cannot have all of its chunks backed up (failure to backup) notify of failure and send delete message for that file_id, stopping the other PUTCHUNK tasks

chunkno:
- max 6 chars (file max size 64GB lmao)

replicationdeg:
- max 9: ignorar as malformed e limitar os args no RMI

chunk size: 64KByte