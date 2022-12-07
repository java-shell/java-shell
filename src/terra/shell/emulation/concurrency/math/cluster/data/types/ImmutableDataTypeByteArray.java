package terra.shell.emulation.concurrency.math.cluster.data.types;

import terra.shell.emulation.concurrency.math.cluster.data.ImmutableDataType;

public class ImmutableDataTypeByteArray implements ImmutableDataType<byte[]> {
	private byte[] data;

	public ImmutableDataTypeByteArray(byte[] data) {
		this.data = data;
	}

	@Override
	public byte[] getData() {
		return data;
	}

}
