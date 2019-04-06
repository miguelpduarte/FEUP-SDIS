chunkno:
- max 6 chars (file max size 64GB lmao)

replicationdeg:
- max 9: ignorar as malformed e limitar os args no RMI

chunk size: 64KByte

todo: consider protocol version and other things like that in message parsing (body is after first two consecutive CRLFs):
- ~Reading body from correct CRLFs~ - done
- Not yet looking at the protocol version

todo: Discuss and change implementation of file\_name to file\_id hashing:
- Currently only the file name and sender id are being considered due to the desire to be able to re-encode without needing the original file
- However, this does not consider different absolute paths (probably should hash using it instead of simply the file nameO)
discuss discuss discuss

todo: not loading the whole file at once, split it directly when reading - must change structure to start sending tasks as the file chunks are read for it to make any difference :/

todo: move chunk\_no to children or intermediate abstract?

todo: discuss stringbuilder to string? (MessageFactory)

~todo: delete must also look at the peer's own files lol <- Very easy but important~ - done but cannot check due to the sender id field encoding the file id
