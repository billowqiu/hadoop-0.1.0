package org.apache.hadoop.dfs.raft;

import com.google.protobuf.InvalidProtocolBufferException;
import net.data.technology.jraft.RaftMessageSender;
import net.data.technology.jraft.Snapshot;
import net.data.technology.jraft.StateMachine;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.apache.hadoop.io.UTF8;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.hadoop.dfs.proto.Journal;

public class JournalStateMachine implements StateMachine {
    static final private Logger logger = LoggerFactory.getLogger(JournalStateMachine.class);
    private URI localEndpoint;
    private JournalRaftServer journalRaftServer;
    public JournalStateMachine(URI localEndpoint, JournalRaftServer journalRaftServer) {
        this.localEndpoint = localEndpoint;
        this.journalRaftServer = journalRaftServer;
    }
    @Override
    public void start(RaftMessageSender raftMessageSender) {
        logger.info("statemachine {} start {}", localEndpoint.toString(), raftMessageSender);
    }

    @Override
    public void commit(long logIndex, byte[] data) {
        try {
            Journal.JournalEntry journalEntry = Journal.JournalEntry.parseFrom(data);
            logger.info("statemachine {} commit logIndex {}, entry {}", localEndpoint.toString(), logIndex, journalEntry);
            switch (journalEntry.getTypeValue()) {
                case Journal.EntryType.MKDIR_VALUE: {
                    journalRaftServer.getNamesystem().mkdirs(new UTF8(journalEntry.getMkdirEntry().getSrc()));
                    break;
                } case Journal.EntryType.DELETE_VALUE: {
                    journalRaftServer.getNamesystem().delete(new UTF8(journalEntry.getDeleteEntry().getSrc()));
                    break;
                } case Journal.EntryType.RENAME_VALUE: {
                    journalRaftServer.getNamesystem().renameTo(new UTF8(journalEntry.getRenameEntry().getSrc()), new UTF8(journalEntry.getRenameEntry().getDst()));
                    break;
                }
                default: {
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rollback(long logIndex, byte[] data) {
        logger.info("statemachine {} rollback logIndex {}, data {}", localEndpoint.toString(), logIndex, data);
    }

    @Override
    public void preCommit(long logIndex, byte[] data) {
        logger.info("statemachine {} preCommit logIndex {}, data {}", localEndpoint.toString(), logIndex, data);
    }

    @Override
    public void saveSnapshotData(Snapshot snapshot, long offset, byte[] data) {
        logger.info("statemachine {} saveSnapshotData offset {}, data {}", localEndpoint.toString(), offset, data);
    }

    @Override
    public boolean applySnapshot(Snapshot snapshot) {
        logger.info("statemachine {} applySnapshot", localEndpoint.toString());
        return false;
    }

    @Override
    public int readSnapshotData(Snapshot snapshot, long offset, byte[] buffer) {
        logger.info("statemachine {} readSnapshotData", localEndpoint.toString());
        return 0;
    }

    @Override
    public Snapshot getLastSnapshot() {
        logger.info("statemachine {} getLastSnapshot", localEndpoint.toString());
        return null;
    }

    @Override
    public CompletableFuture<Boolean> createSnapshot(Snapshot snapshot) {
        logger.info("statemachine {} createSnapshot", localEndpoint.toString());
        return null;
    }

    @Override
    public void exit(int code) {
        logger.info("statemachine {} exit", localEndpoint.toString());
    }
}
